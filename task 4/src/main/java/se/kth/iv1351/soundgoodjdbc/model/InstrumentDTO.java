package se.kth.iv1351.soundgoodjdbc.model;
/* 
 * DATA TRANSFER OBJECT (DTO)
 * specifies a read-only view of an Instrument.
 */
public interface InstrumentDTO {
    
    /**
     * @return The instrument's unique identifier.
     */
    String getInstrumentID();

    /**
     * @return The type of the instrument (e.g., Guitar, Piano).
     */
    String getInstrumentType();

    /**
     * @return The brand of the instrument (e.g., Yamaha, Gibson).
     */
    String getInstrumentBrand();

    /**
     * @return The price of the instrument.
     */
    double getInstrumentPrice();

    /**
     * @return The number of instruments available for rent.
     */
    int getAvailableStock();
}
