package tourGuide.exception;

import feign.Response;
import feign.codec.ErrorDecoder;


public class CustomErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String invoqueur, Response reponse) {

        String [] message = invoqueur.split("#");
        message[1] = message[1].replace("get", "") + " not obtained";
        if(reponse.status() == 404 ) {
            return new NotFoundException(message[0] + message[1]);
        }

        return defaultErrorDecoder.decode(invoqueur, reponse);
    }

}

