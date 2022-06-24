package com.nazeem.multidb.mongodb.dto;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import lombok.*;

import java.util.Date;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class AvantLinkReturnsFeedData {
    public static String[] columns = new String[]
            { "Name", "Address", "Email", "ContactNo", "Total Amount","Created Date", "Order Status" };

    @CsvBindByPosition(position = 0)
    @CsvBindByName(column = "Customer name")
    private String customerName;

    @CsvBindByPosition(position = 1)
    @CsvBindByName(column = "Customer address")
    private String customerAddress;


    @CsvBindByPosition(position = 2)
    @CsvBindByName(column = "Customer Email")
    private String customerEmail;

    @CsvBindByPosition(position = 3)
    @CsvBindByName(column = "Customer Contact no")
    private String customerContactNo;


    @CsvBindByPosition(position = 4)
    @CsvBindByName(column = "Total Amount")
    private Double totalAmount;

    @CsvBindByPosition(position = 5)
    @CsvBindByName(column = "Created Date")
    private Date createdDate;


    @CsvBindByPosition(position = 6)
    @CsvBindByName(column = "Order Status")
    private String orderStatus;
}
