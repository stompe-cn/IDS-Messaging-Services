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
package ids.messaging.handler.message;

import de.fraunhofer.iais.eis.Message;
import ids.messaging.response.MessageResponse;

/**
 * Interface for implementing message handler for specific message types.
 * @param <T> The type of message that the handler should handle.
 */
public interface MessageHandler<T extends Message> {

    /**
     * Handle an incoming Message of type T and return a MessageResponse.
     *
     * @param queryHeader Header part of the incoming Message (an instance of RequestMessage).
     * @param payload Payload of the Message (as MessagePayload, access with
     *                getUnderlyingInputStream()).
     * @return An instance of MessageResponse (BodyResponse, ErrorResponse,...).
     * @throws MessageHandlerException If an error occurs while handling the incoming message.
     */
    MessageResponse handleMessage(T queryHeader, MessagePayload payload)
            throws MessageHandlerException;
}
