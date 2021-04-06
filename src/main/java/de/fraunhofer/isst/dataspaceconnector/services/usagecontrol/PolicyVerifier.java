package de.fraunhofer.isst.dataspaceconnector.services.usagecontrol;

import com.tecnalia.datausage.utils.HttpUtils;
import de.fraunhofer.iais.eis.Constraint;
import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.Rule;
import java.io.UnsupportedEncodingException;
//import de.fraunhofer.isst.dataspaceconnector.services.messages.implementation.LogMessageService;
//import de.fraunhofer.isst.dataspaceconnector.services.messages.implementation.NotificationMessageService;
//import de.fraunhofer.isst.dataspaceconnector.services.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.Duration;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * This class provides access permission information for the
 * {@link de.fraunhofer.isst.dataspaceconnector.services.usagecontrol.PolicyHandler} depending on the policy content.
 */
@Component
public class PolicyVerifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyVerifier.class);

    private final PolicyReader policyReader;
    //private final NotificationMessageService notificationMessageService;
    //private final LogMessageService logMessageService;
    private final HttpUtils httpUtils;

    /**
     * Constructor for PolicyVerifier.
     *
     * @throws IllegalArgumentException if any of the parameters is null.
     */
    @Autowired
//    public PolicyVerifier(PolicyReader policyReader, LogMessageService logMessageService,
//        NotificationMessageService notificationMessageService, HttpUtils httpUtils)
    public PolicyVerifier(PolicyReader policyReader, HttpUtils httpUtils)
        throws IllegalArgumentException {
        if (policyReader == null)
            throw new IllegalArgumentException("The PolicyReader cannot be null.");

        /*if (logMessageService == null)
            throw new IllegalArgumentException("The LogMessageService cannot be null.");

        if (notificationMessageService == null)
            throw new IllegalArgumentException("The NotificationMessageService cannot be null.");*/

        if (httpUtils == null)
            throw new IllegalArgumentException("The HttpUtils cannot be null.");

        this.policyReader = policyReader;
        /*this.logMessageService = logMessageService;
        this.notificationMessageService = notificationMessageService;*/
        this.httpUtils = httpUtils;
    }

    /**
     * Allows data access.
     *
     * @return true.
     */
    @SuppressWarnings("SameReturnValue")
    public boolean allowAccess() {
        return true;
    }

    /**
     * Inhibits data access.
     *
     * @return false.
     */
    @SuppressWarnings("SameReturnValue")
    public boolean inhibitAccess() {
        return false;
    }

    /**
     * Saves the access date into the database and allows the access only if that operation was successful.
     * TODO: Validate response in more detail.
     * TODO: Add log message.
     *
     * @return true, if the access was logged; false otherwise.
     */
    public boolean logAccess() {
        //TODO: PLATOON
        Map<String, String> response;
        try {
            //TODO: PLATOON - Log Access
            //response = logMessageService.sendRequestMessage("");
            return allowAccess();
        } catch (Exception exception) {
            LOGGER.warn("Log message could not be sent. [exception=({})]", exception.getMessage());
            return allowAccess();
        }
        /*if (response != null) {
            return allowAccess();
        } else {
            LOGGER.warn("No response received.");
            return allowAccess();
        }*/
    }


    /**
     * Checks if the requested access is in the allowed time interval.
     *
     * @param permissionList a list of {@link de.fraunhofer.iais.eis.Permission} objects.
     * @return true, if the current date is within the time interval; false otherwise.
     */
    public boolean checkInterval(ArrayList<Permission> permissionList) {
        PolicyReader.TimeInterval timeInterval = policyReader
            .getTimeInterval(permissionList.get(0));
        Date date = new Date();

        if (date.after(timeInterval.getStart()) && date.before(timeInterval.getEnd())) {
            return allowAccess();
        } else {
            return inhibitAccess();
        }
    }

    /**
     * Checks whether the current date is later than the specified one.
     *
     * @param dateNow   the current date.
     * @param maxAccess the target date.
     * @return true, if the current date is later than the target date; false otherwise.
     */
    public boolean checkDate(Date dateNow, Date maxAccess) {
        return dateNow.after(maxAccess);
    }

    /**
     * Adds a duration to a given date and checks if the duration has already been exceeded.
     *
     * @param created        the date when the resource was created.
     * @param permissionList a list of {@link de.fraunhofer.iais.eis.Permission} objects..
     * @return true, the duration has not been exceeded; false otherwise.
     */
    public boolean checkDuration(Date created, ArrayList<Permission> permissionList) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(created);
        try {
            Duration duration = policyReader.getDuration(permissionList.get(0));

            cal.add(Calendar.SECOND, duration.getSeconds());
            cal.add(Calendar.MINUTE, duration.getMinutes());
            cal.add(Calendar.HOUR_OF_DAY, duration.getHours());
            cal.add(Calendar.DAY_OF_MONTH, duration.getDays());
            cal.add(Calendar.MONTH, duration.getMonths());
            cal.add(Calendar.YEAR, duration.getYears());

            return !checkDate(new Date(), cal.getTime());
        } catch (DatatypeConfigurationException e) {
            return inhibitAccess();
        }
    }

    /**
     * Checks whether the maximum number of accesses has already been reached.
     *
     * @param permissionList a list of {@link de.fraunhofer.iais.eis.Permission} objects.
     * @param targetId       the data target URI.
     * @return true, if the maximum number of accesses has not been reached yet; false otherwise.
     */
    public boolean checkFrequency(ArrayList<Permission> permissionList, String targetId) {
        int max = policyReader.getMaxAccess(permissionList.get(0));
        URI pip = policyReader.getPipEndpoint(permissionList.get(0));
        
        try {
            String encodedTargetUri =  URLEncoder.encode(targetId, StandardCharsets.UTF_8.toString());
            String accessed = httpUtils.sendHttpGetRequest(
                    pip + "?targetUri="+ encodedTargetUri);
            if (Integer.parseInt(accessed) >= max) {
                return inhibitAccess();
            } else {
                return allowAccess();
            }
        } catch (UnsupportedEncodingException | URISyntaxException | RuntimeException e) {
            return inhibitAccess();
        }
    }

    /**
     * Checks whether the consumer Role is allowed to access the data.
     *
     * @param permissionList a list of {@link de.fraunhofer.iais.eis.Permission} objects.
     * @param consumerURI    the consumer URI.
     * @return true, if the consumer Role is allowed to access the data; false otherwise.
     */
    public boolean checkRole(ArrayList<Permission> permissionList, URI consumerURI) {
        Rule rule = permissionList.get(0);
        URI allowedRoleURI = policyReader.getAllowedRole(rule);
        String allowedRoleAsString = allowedRoleURI.toString();
        URI pip = policyReader.getPipEndpoint(permissionList.get(0));
        
        try {
            String encodedConsumerUri =  URLEncoder.encode(consumerURI.toString(), StandardCharsets.UTF_8.toString());
            String consumerRoleAsString = httpUtils.sendHttpGetRequest(
                    pip + "?consumerUri="+ encodedConsumerUri);
            if (allowedRoleAsString.equals(consumerRoleAsString)) {
                return allowAccess();
            } else {
                return inhibitAccess();
            }
        } catch (UnsupportedEncodingException | URISyntaxException | RuntimeException e) {
            return inhibitAccess();
        }
    }

    /**
     * Checks whether the consumerś Purpose is allowed to access the data.
     *
     * @param permissionList a list of {@link de.fraunhofer.iais.eis.Permission} objects.
     * @param consumerURI    the consumer URI.
     * @return true, if the consumerś Purpose is allowed to access the data; false otherwise.
     */
    public boolean checkPurpose(ArrayList<Permission> permissionList, URI consumerURI) {
        Rule rule = permissionList.get(0);
        URI allowedPurposeURI = policyReader.getAllowedPurpose(rule);
        String allowedPurposeAsString = allowedPurposeURI.toString();
        URI pip = policyReader.getPipEndpoint(permissionList.get(0));
        
        try {
            String encodedConsumerUri =  URLEncoder.encode(consumerURI.toString(), StandardCharsets.UTF_8.toString());
            String consumerPurposeAsString = httpUtils.sendHttpGetRequest(
                    pip + "?consumerUri="+ encodedConsumerUri);
            if (allowedPurposeAsString.equals(consumerPurposeAsString)) {
                return allowAccess();
            } else {
                return inhibitAccess();
            }
        } catch (UnsupportedEncodingException | URISyntaxException | RuntimeException e) {
            return inhibitAccess();
        }
    }
}
