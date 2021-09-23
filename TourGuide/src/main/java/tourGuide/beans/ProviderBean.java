package tourGuide.beans;

import java.util.UUID;

public class ProviderBean {
    public final String name;
    public final double price;
    public final UUID tripId;

    public ProviderBean(UUID tripId, String name, double price) {
        this.name = name;
        this.tripId = tripId;
        this.price = price;
    }

}
