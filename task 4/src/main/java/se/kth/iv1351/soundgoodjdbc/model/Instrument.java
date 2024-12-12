package se.kth.iv1351.soundgoodjdbc.model;

/**
 * This class represents an Instrument and implements the InstrumentDTO interface.
 */
public class Instrument implements InstrumentDTO {
    private String id;
    private String type;
    private String brand;
    private double price;
    private int availableStock;

    public Instrument(String id, String type, String brand, double price, int availableStock) {
        this.id = id;
        this.type = type;
        this.brand = brand;
        this.price = price;
        this.availableStock = availableStock;
    }

    // Implementing InstrumentDTO methods
    @Override
    public String getInstrumentID() {
        return id;
    }

    @Override
    public String getInstrumentType() {
        return type;
    }

    @Override
    public String getInstrumentBrand() {
        return brand;
    }

    @Override
    public double getInstrumentPrice() {
        return price;
    }

    @Override
    public int getAvailableStock() {
        return availableStock;
    }

    @Override
    public String toString() {
        return "Instrument{id='" + id + "', type='" + type + "', brand='" + brand + "', price=" + price +
               ", availableStock=" + availableStock + "}";
    }
}