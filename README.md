# MyBand

## Summary
Myband is an android app which goal is to make a real-time reading of a Microsoft Band 2 sensors and export their values to a csv file.  It has a single activity where sensors are displayed, with a "Save Metrics" button , to export the sensor readings to a .csv file. 

![myband_ic](https://user-images.githubusercontent.com/61889565/93129198-7c04e400-f685-11ea-9746-a1705fffe588.png)



## Configuration
As a pre-requisite, ensure your Microsoft Band 2 is configured for use on your android device.

Once the Microsoft Band 2 is bounded via bluetooth, the user needs to wear it using the position shown on the next image.

![bandPositionSmall](https://user-images.githubusercontent.com/61889565/92423971-d955de00-f137-11ea-970f-2356c3f950de.jpg)

## Use
The app has one single activity,  the "Main Activity", which layout is shown in the screenshot below:

![main_activity](https://user-images.githubusercontent.com/61889565/93125141-8d4af200-f67f-11ea-8ce6-c2721ba8a0f3.png)

It has a 'Settings' option where the output csv file format can be adjusted. 

![csv_settings](https://user-images.githubusercontent.com/61889565/93125139-8cb25b80-f67f-11ea-8985-fc1320ed2761.png)

'Frquency based' csv file: it exports a csv file for each selected sampling rate. 

'Time based' csv file: it exports a single csv file with all sensor readings ordered by their timestamp.

'Sample based' csv file: it exports  a single csv file, concatenating all readings into a single row according to their timestamp.

First, the values need to be read without 'recording' them into the output csv file. Then, the  'Save Metrics' button needs to be pressed in order to start 'recording' the sensor values into the database. When the time is up, all the values previously written to the database are written into a csv file.

![reading_values](https://user-images.githubusercontent.com/61889565/93125132-8b812e80-f67f-11ea-8f96-4c90d293885c.png)
![recording_readings](https://user-images.githubusercontent.com/61889565/93125133-8c19c500-f67f-11ea-9dc7-b7dc9d9885d5.png)
![saving_csv](https://user-images.githubusercontent.com/61889565/93125134-8c19c500-f67f-11ea-8715-a98eb6c96b7a.png)
![csv_saved](https://user-images.githubusercontent.com/61889565/93125136-8cb25b80-f67f-11ea-8712-b025c10d463b.png)

## Result

As a result, a csv file is exported to the root directory.

![outputfile](https://user-images.githubusercontent.com/61889565/93125127-8a500180-f67f-11ea-8448-99a838ff0ccc.JPG)
