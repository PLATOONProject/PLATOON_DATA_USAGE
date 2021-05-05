package de.fraunhofer.isst.dataspaceconnector.services.usagecontrol;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.isst.dataspaceconnector.exceptions.contract.UnsupportedPatternException;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class provides policy pattern recognition and calls the {@link
 * de.fraunhofer.isst.dataspaceconnector.services.usagecontrol.PolicyVerifier} on data request or
 * access.
 */
@Component
public class PolicyHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyHandler.class);

    private final PolicyVerifier policyVerifier;

    /**
     * Constructor for PolicyHandler.
     *
     * @throws IllegalArgumentException if any of the parameters is null.
     */
    @Autowired
    public PolicyHandler(PolicyVerifier policyVerifier)
        throws IllegalArgumentException {
        if (policyVerifier == null)
            throw new IllegalArgumentException("The PolicyVerifier cannot be null.");

        this.policyVerifier = policyVerifier;
    }

    /**
     * Reads the properties of an ODRL policy to automatically recognize the policy pattern.
     *
     * @param permissionList  a list of {@link de.fraunhofer.iais.eis.Permission} objects.
     * @param prohibitionList a list of {@link de.fraunhofer.iais.eis.Prohibition} objects.
     * @return the recognized policy pattern.
     * @throws UnsupportedPatternException if no pattern could be recognized.
     */
    public Pattern getPattern(ArrayList<Permission> permissionList, ArrayList<Prohibition> prohibitionList) {

        if (prohibitionList != null && prohibitionList.size() > 0 && prohibitionList.get(0) != null) {
            return Pattern.PROHIBIT_ACCESS;
        }

        if (permissionList != null && permissionList.size() > 0 && permissionList.get(0) != null) {
            Permission permission = permissionList.get(0);
            //Action action = permission.getAction().get(0);
            //if(action.)
            List<AbstractConstraint> constraints = permission.getConstraint();
            List<Duty> postDuties = permission.getPostDuty();

            Action ruleAction = permission.getAction().get(0);
            if (ruleAction != Action.USE) {
                throw new UnsupportedPatternException(
                    "The recognized policy pattern is not supported by this connector.");
            }
            if (constraints != null && constraints.get(0) != null) {
                if (constraints.size() > 1) {
                    return Pattern.USAGE_DURING_INTERVAL;
                } else {
                    LeftOperand leftOperand = ((Constraint)constraints.get(0)).getLeftOperand();
                    if (leftOperand == LeftOperand.COUNT) {
                        return Pattern.N_TIMES_USAGE;
                    } else if (leftOperand == LeftOperand.ELAPSED_TIME) {
                        return Pattern.DURATION_USAGE;
                    } else if ((leftOperand == LeftOperand.USER )
                            && (((Constraint)constraints.get(0)).getOperator() == BinaryOperator.HAS_MEMBERSHIP))  {
                        return Pattern.ROLE_RESTRICTED_USAGE;
                    } else if ((leftOperand == LeftOperand.PURPOSE )
                            && (((Constraint)constraints.get(0)).getOperator() == BinaryOperator.SAME_AS))  {
                        return Pattern.PURPOSE_RESTRICTED_USAGE;
                    } else {
                        throw new UnsupportedPatternException(
                            "The recognized policy pattern is not supported by this connector.");
                    }
                }
            } else {
                if (postDuties != null && postDuties.get(0) != null) {
                    Action action = postDuties.get(0).getAction().get(0);
                    if (action == Action.LOG) {
                        return Pattern.USAGE_LOGGING;
                    } else {
                        throw new UnsupportedPatternException(
                            "The recognized policy pattern is not supported by this connector.");
                    }
                } else {
                    return Pattern.PROVIDE_ACCESS;
                }
            }
        } else {
            throw new UnsupportedPatternException(
                "The recognized policy pattern is not supported by this connector.");
        }
    }

    /**
     * Implements the policy restrictions depending on the policy pattern type on data provision (as provider).
     *
     * @param permissionList  a list of {@link de.fraunhofer.iais.eis.Permission} objects.
     * @param prohibitionList a list of {@link de.fraunhofer.iais.eis.Prohibition} objects.
     * @return whether the data can be provided.
     * @throws UnsupportedPatternException if no pattern could be recognized.
     */
    public boolean onDataProvision(ArrayList<Permission> permissionList, ArrayList<Prohibition> prohibitionList, String consumerURI) throws UnsupportedPatternException {
        switch (getPattern(permissionList, prohibitionList)) {
            case PROVIDE_ACCESS:
                return policyVerifier.allowAccess();
            case PROHIBIT_ACCESS:
                return policyVerifier.inhibitAccess();
            case USAGE_DURING_INTERVAL:
                return policyVerifier.checkInterval(permissionList);
            default:
                return true;
        }
    }

    /**
     * Implements the policy restrictions depending on the policy pattern type on data access (as consumer).
     *
     * @param permissionList  a list of {@link de.fraunhofer.iais.eis.Permission} objects.
     * @param prohibitionList a list of {@link de.fraunhofer.iais.eis.Prohibition} objects.
     * @param resourceCreated the date when the resource was created.
     * @param targetId        the URI of the target data.
     * @return whether the data can be accessed.
     * @throws UnsupportedPatternException if no pattern could be recognized.
     */
    public boolean onDataAccess(ArrayList<Permission> permissionList, ArrayList<Prohibition> prohibitionList, Date resourceCreated, String targetId, String consumerURI) throws UnsupportedPatternException {
        final var ignoreUnsupportedPatterns = false;

        Pattern pattern;
        try {
            pattern = getPattern(permissionList, prohibitionList);
        } catch (UnsupportedPatternException exception) {
            if (!ignoreUnsupportedPatterns)
                throw new UnsupportedPatternException(exception.getMessage());
            else
                pattern = Pattern.PROVIDE_ACCESS;
        }

        switch (pattern) {
            case USAGE_DURING_INTERVAL:
                return policyVerifier.checkInterval(permissionList);
            case DURATION_USAGE:
                return policyVerifier.checkDuration(resourceCreated, permissionList);
            case USAGE_LOGGING:
                return policyVerifier.logAccess();
            case N_TIMES_USAGE:
                return policyVerifier.checkFrequency(permissionList, targetId, consumerURI);
            case ROLE_RESTRICTED_USAGE:
                return policyVerifier.checkRole(permissionList, consumerURI);
            case PURPOSE_RESTRICTED_USAGE:
                return policyVerifier.checkPurpose(permissionList, consumerURI);
            default:
                return true;
        }
    }


    public enum Pattern {
        /**
         * Standard pattern to allow unrestricted access.
         */
        PROVIDE_ACCESS("PROVIDE_ACCESS"),
        /**
         * Default pattern if no other is detected. v2.0: NO_POLICY("no-policy")
         */
        PROHIBIT_ACCESS("PROHIBIT_ACCESS"),
        /**
         * Type: NotMoreThanN v2.0: COUNT_ACCESS("count-access") https://github.com/International-Data-Spaces-Association/InformationModel/blob/master/examples/contracts-and-usage-policy/templates/NTimesUsageTemplates/N_TIMES_USAGE_OFFER_TEMPLATE.jsonld
         */
        N_TIMES_USAGE("N_TIMES_USAGE"),
        /**
         * Type: DurationOffer https://github.com/International-Data-Spaces-Association/InformationModel/blob/master/examples/contracts-and-usage-policy/templates/TimeRestrictedUsageTemplates/DURATION_USAGE_OFFER_TEMPLATE.jsonld
         */
        DURATION_USAGE("DURATION_USAGE"),
        /**
         * Type: IntervalUsage v2.0: TIME_INTERVAL("time-interval") https://github.com/International-Data-Spaces-Association/InformationModel/blob/master/examples/contracts-and-usage-policy/templates/TimeRestrictedUsageTemplates/USAGE_DURING_INTERVAL_OFFER_TEMPLATE.jsonld
         */
        USAGE_DURING_INTERVAL("USAGE_DURING_INTERVAL"),
        /**
         * Type: Logging v2.0: LOG_ACCESS("log-access") https://github.com/International-Data-Spaces-Association/InformationModel/blob/master/examples/contracts-and-usage-policy/templates/UsageLoggingTemplates/USAGE_LOGGING_OFFER_TEMPLATE.jsonld
         */
        USAGE_LOGGING("USAGE_LOGGING"),
        /**
         * Type: Notification https://github.com/International-Data-Spaces-Association/InformationModel/blob/master/examples/contracts-and-usage-policy/templates/UsageNotificationTemplates/USAGE_NOTIFICATION_OFFER_TEMPLATE.jsonld
         */
        USAGE_NOTIFICATION("USAGE_NOTIFICATION"),

        /**
         * Type: Role based: https://github.com/International-Data-Spaces-Association/InformationModel/blob/master/examples/contracts-and-usage-policy/templates/RolebasedAgreementTemplates/ROLEBASED_AGREEMENT_TEMPLATE.jsonld
         */
        ROLE_RESTRICTED_USAGE("ROLE_RESTRICTED_USAGE"),
        
        /**
         * Type: Purpose restricted: https://github.com/International-Data-Spaces-Association/InformationModel/blob/master/examples/contracts-and-usage-policy/templates/PurposeRestrictedUsageTemplates/PURPOSE_RESTRICTED_USAGE_AGREEMENT_TEMPLATE.jsonld
         */
        PURPOSE_RESTRICTED_USAGE("PURPOSE_RESTRICTED_USAGE");

        private final String pattern;

        Pattern(String string) {
            pattern = string;
        }

        @Override
        public String toString() {
            return pattern;
        }
    }
}
