/*
 * Copyright 2020 Fraunhofer Institute for Software and Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dataspaceconnector.services.usagecontrol;

import de.fraunhofer.iais.eis.Rule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Date;

//TECNALIA-ICT-OPTIMA: Different class attributes: 
// - consumerUri instead of issuerConnector
// - created: ContractAgreement start date
// - rules instead of agreement
@AllArgsConstructor
@Data
@RequiredArgsConstructor
public class VerificationInput {

    /**
     * The id of the targeted artifact.
     */
    String target;

    /**
     * The list of rules.
     */
    ArrayList<Rule> rules;
    
    /**
     * The id of the consumer connector.
     */
    String consumerUri;
    
    /**
     * The start date of the ContractAgreement.
     */
    Date created;
     
}
