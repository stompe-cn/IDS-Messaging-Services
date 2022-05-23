/*
 * Copyright Fraunhofer Institute for Software and Systems Engineering
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
 *
 *  Contributors:
 *       sovity GmbH
 *
 */
package ids.messaging.requests.builder;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import de.fraunhofer.iais.eis.util.TypedLiteral;
import ids.messaging.common.DeserializeException;
import ids.messaging.common.SerializeException;
import ids.messaging.core.daps.ClaimsException;
import ids.messaging.core.daps.DapsTokenManagerException;
import ids.messaging.protocol.MessageService;
import ids.messaging.protocol.http.SendMessageException;
import ids.messaging.protocol.http.ShaclValidatorException;
import ids.messaging.protocol.multipart.UnknownResponseException;
import ids.messaging.protocol.multipart.parser.MultipartParseException;
import ids.messaging.requests.MessageContainer;
import ids.messaging.requests.NotificationTemplateProvider;
import ids.messaging.requests.RequestTemplateProvider;
import ids.messaging.requests.enums.Crud;
import ids.messaging.requests.enums.ProtocolType;
import ids.messaging.requests.exceptions.RejectionException;
import ids.messaging.requests.exceptions.UnexpectedPayloadException;

/**
 * RequestBuilder for messages with subject 'connector certificate'.
 *
 * @param <T> Type of expected Payload.
 */
public class ConnectorCertificateRequestBuilder<T> extends IdsRequestBuilder<T>
        implements ExecutableBuilder<T>,
        SupportsMultipart<T, ConnectorCertificateRequestBuilder<T>> {

    /**
     * URI of the affected connector.
     */
    private URI affectedConnector;

    /**
     * Reason for certificate revocation.
     */
    private TypedLiteral revocationReason;

    ConnectorCertificateRequestBuilder(
            final Class<T> expected,
            final MessageService messageService,
            final RequestTemplateProvider requestTemplateProvider,
            final NotificationTemplateProvider notificationTemplateProvider) {
        super(expected, messageService, requestTemplateProvider, notificationTemplateProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectorCertificateRequestBuilder<T> withPayload(final Object payload) {
        this.optPayload = Optional.ofNullable(payload);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectorCertificateRequestBuilder<T> throwOnRejection() {
        this.throwOnRejection = true;
        return this;
    }

    /**
     * Set the operation to UPDATE: describes a
     * {@link de.fraunhofer.iais.eis.ConnectorCertificateGrantedMessage}.
     *
     * @param affectedConnector Affected connector id for message header.
     * @return This builder instance.
     */
    public ConnectorCertificateRequestBuilder<T> operationUpdate(final URI affectedConnector) {
        this.operation = Crud.UPDATE;
        this.affectedConnector = affectedConnector;
        return this;
    }

    /**
     * Set the operation to DELETE: describes a
     * {@link de.fraunhofer.iais.eis.ConnectorCertificateRevokedMessage}.
     *
     * @param affectedConnector Affected connector id for message header.
     * @param revocationReason Reason why certificate was revoked.
     * @return This builder instance.
     */
    public ConnectorCertificateRequestBuilder<T> operationDelete(
            final URI affectedConnector, final TypedLiteral revocationReason) {
        this.operation = Crud.DELETE;
        this.affectedConnector = affectedConnector;
        this.revocationReason = revocationReason;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageContainer<T> execute(final URI target)
            throws DapsTokenManagerException,
            ShaclValidatorException,
            SerializeException,
            ClaimsException,
            UnknownResponseException,
            SendMessageException,
            MultipartParseException,
            IOException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException {
        if (protocolType == null || operation == null) {
            final var errorMessage = String.format(
                    "Could not send Message, needed Fields are null: %s%s",
                    protocolType == null ? "protocolType is null! " : "",
                    operation == null ? "operation is null! " : ""
            );
            throw new SendMessageException(errorMessage);
        }
        switch (protocolType) {
            case IDSCP:
                throw new UnsupportedOperationException("Not yet implemented Protocol!");
            case LDP:
                throw new UnsupportedOperationException("Not yet implemented Protocol!");
            case MULTIPART:
                switch (operation) {
                    case UPDATE:
                        final var updateMessage = notificationTemplateProvider
                                .connectorCertificateGrantedMessageTemplate(
                                        affectedConnector)
                                .buildMessage();
                        return sendMultipart(target, updateMessage);
                    case DELETE:
                        final var deleteMessage = notificationTemplateProvider
                                .connectorCertificateRevokedMessageTemplate(
                                    affectedConnector, revocationReason)
                                .buildMessage();
                        return sendMultipart(target, deleteMessage);
                    default:
                        throw new UnsupportedOperationException("Unsupported Operation!");
                }
            default:
                throw new UnsupportedOperationException("Unsupported Protocol!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectorCertificateRequestBuilder<T> useMultipart() {
        this.protocolType = ProtocolType.MULTIPART;
        return this;
    }
}
