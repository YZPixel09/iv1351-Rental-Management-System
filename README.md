# iv1351-soundgoodjdbc
 Rental Management System for Soundgood Music School

This is a Java language project for the IV1351 course, developed for the Soundgood Music School rental system. Through interactive operations, the code connects to the database to achieve the following functionalities:

1. List available instruments
2. Rent an instrument
3. Terminate a rental
4. Check student rentals

Example 1: First, attempt to check the information for the violin category. The initial inventory displays data related to violins, including instrument ID, brand, price, and stock quantity.  

![](screenshots/screenshot1.png)

Proceed to rent a violin for the student with ID 3. After displaying a success message, check the violin information again. At this point, the stock in the database has decreased to 1.  The lease expiration date will automatically be displayed as 30 days in the future.

Example 2: Attempt to terminate the rental with Rental ID R001. Upon checking the database, a screenshot confirms that the rental has been terminated at the time of our operation.  

![](screenshots/screenshot2.png)

Check the rental status for the student with ID 2. The displayed data matches the database records perfectly.  

With this, all four functionalities have been successfully tested.
![](screenshots/screenshot3.png)
