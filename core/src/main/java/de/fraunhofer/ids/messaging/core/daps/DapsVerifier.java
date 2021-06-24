/*
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
package de.fraunhofer.ids.messaging.core.daps;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.fraunhofer.ids.messaging.core.daps.customvalidation.DatValidationRule;
import de.fraunhofer.ids.messaging.core.daps.customvalidation.ValidationRuleException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * The DefaultVerifier contains some default DAPS verification rules.
 */
@Slf4j
@UtilityClass
public class DapsVerifier {

    /**
     * Custom DAT validation rules, which are checked additionally to the default checks.
     */
    @SuppressWarnings("FieldMayBeFinal")
    private static List<DatValidationRule> datValidationRules = new ArrayList<>();

    /**
     * Add a custom validation rule to check the DAT.
     *
     * @param datValidationRule {@link DatValidationRule} to add
     */
    public void addValidationRule(final DatValidationRule datValidationRule) {
        datValidationRules.add(datValidationRule);
    }


    /**
     * Check notbefore and expiration of the DAT Token Claims.
     * The default rules check if the current Time is between NotBefore and Expiration
     *
     * @param toVerify the claims to verify
     * @return true if message is valid
     * @throws ClaimsException when the claims of the DAT cannot be verified
     */
    public boolean verify(final Jws<Claims> toVerify) throws ClaimsException {
        if (toVerify != null) {
            return verify(toVerify.getBody());
        }
        throw new ClaimsException("Could not verify claims, input was null!");
    }

    /**
     * Check notbefore and expiration of the DAT Token Claims.
     * The default rules check if the current Time is between NotBefore and Expiration
     *
     * @param toVerify the claims to verify
     * @return true if message is valid
     * @throws ClaimsException when the claims of the DAT cannot be verified
     */
    public boolean verify(final Claims toVerify) throws ClaimsException {
        try {
            if (toVerify.getExpiration().before(new Date())) {
                throw new ClaimsException("The token is outdated.");
            }

            if (toVerify.getExpiration().before(toVerify.getIssuedAt()) || new Date().before(toVerify.getIssuedAt())) {
                throw new ClaimsException("The token's issued time (iat) is invalid");
            }

            if (new Date().before(toVerify.getNotBefore())) {
                throw new ClaimsException("The token's not before time is invalid");
            }
            if (log.isDebugEnabled()) {
                log.debug("Checking custom rules...");
            }
            //check custom dat rules
            if (!datValidationRules.isEmpty()) {
                for (final var rule : datValidationRules) {
                    try {
                        final var result = rule.checkRule(toVerify);

                        if (!result.isSuccess()) {
                            //if a rule fails, reject token
                            if (log.isWarnEnabled()) {
                                log.warn(String.format("Custom DAT validation rule failed! Message: %s", result.getMessage()));
                            }

                            throw new ClaimsException(String.format("Custom Rule failed! Message: %s", result.getMessage()));
                        }
                    } catch (ValidationRuleException e) {
                        //if a rule throws an exception, log exception and reject token
                        if (log.isErrorEnabled()) {
                            log.error("Exception thrown by custom DAT validation rule!");
                            log.error(e.getMessage(), e);
                        }
                        throw new ClaimsException(String.format("Custom Rule threw Exception! Message: %s", e.getMessage()));
                    }
                }
            }
            if (log.isInfoEnabled()) {
                log.info("Claims verified successfully");
            }

            return true;
        } catch (NullPointerException e) {
            if (log.isWarnEnabled()) {
                log.warn("Could not verify Claims of the DAT Token!");
            }

            throw new ClaimsException(e.getMessage());
        }
    }
}
