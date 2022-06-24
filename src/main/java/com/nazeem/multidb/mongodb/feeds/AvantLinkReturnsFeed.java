package com.nazeem.multidb.mongodb.feeds;

import com.nazeem.multidb.mongodb.dto.AvantLinkReturnsFeedData;
import com.nazeem.multidb.mongodb.dto.CustomerOrder;
import com.nazeem.multidb.mongodb.model.Customer;
import com.nazeem.multidb.mongodb.model.Order;
import com.nazeem.multidb.mongodb.service.OrderService;
import com.nazeem.multidb.mongodb.util.FTPSettings;
import com.nazeem.multidb.mongodb.util.FTPUpload;
import com.nazeem.multidb.mongodb.util.IFeedsActions;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class AvantLinkReturnsFeed implements IFeedsActions {
    private static final Logger logger = Logger.getLogger(AvantLinkReturnsFeed.class.getName());

    @Autowired
    private OrderService orderService;

    @Autowired
    private FTPUpload ftpUpload;


    //Reading data from application.properties file
    @Value("${ftp.avantlink.returns.server}")
    private String FTP_SERVER;

    @Value("${ftp.avantlink.returns.port}")
    private int FTP_PORT;

    @Value("${ftp.avantlink.returns.username}")
    private String FTP_USERNAME;

    @Value("${ftp.avantlink.returns.password}")
    private String FTP_PASSWORD;

    @Value("${ftp.avantlink.returns.directory}")
    private String FTP_DIRECTORY;

    @Value("${save.csv.folder}")
    private String SAVE_CSV_FOLDER;

    @Value("${uploaded.csv.folder}")
    private String UPLOADED_CSV_FOLDER;

    //%s timestamp
    private String CSV_FILE_NAME="AvantLink_Returns_%s.csv";



    @Override
    public Object fetch(Object o) {
        String customerId = (String) o;
        logger.info("Avant link returns - fetching data...");
        return orderService.findCustomerOrders(customerId);
    }

    @Override
    public Object convert(Object o) {
        CustomerOrder customerOrder = (CustomerOrder) o;

        logger.info("Avant link returns - converting data...");

        List<AvantLinkReturnsFeedData> feed1DataList = new ArrayList<>();
        //Get customer
        Customer customer = customerOrder.getCustomer();
        //Get orders
        for(Order order : customerOrder.getOrder()){
            AvantLinkReturnsFeedData avantLinkSalesFeedData = new AvantLinkReturnsFeedData();
            //Set customer data
            avantLinkSalesFeedData.setCustomerName(customer.getFullName());
            avantLinkSalesFeedData.setCustomerAddress(customer.getAddress());
            avantLinkSalesFeedData.setCustomerContactNo(customer.getContactNo());
            avantLinkSalesFeedData.setCustomerEmail(customer.getEmail());
            //Set order data

            avantLinkSalesFeedData.setOrderStatus(order.getOrderStatus());
            avantLinkSalesFeedData.setCreatedDate(order.getCreatedDate());
            avantLinkSalesFeedData.setTotalAmount(order.getTotalAmount());

            feed1DataList.add(avantLinkSalesFeedData);
        }
        return feed1DataList;
    }

    @Override
    public File save(Object o) {
        List<AvantLinkReturnsFeedData> feed1DataList = (List<AvantLinkReturnsFeedData>) o;

        logger.info("Avant link returns - saving file...");

        // name of generated csv
        final String CSV_LOCATION = SAVE_CSV_FOLDER +"/"+ String.format(CSV_FILE_NAME, new Date());
        File file=new File(CSV_LOCATION);
        try {

            // Creating writer class to generate
            // csv file
            FileWriter writer = new FileWriter(file);

            // Create Mapping Strategy to arrange the
            // column name in order
            ColumnPositionMappingStrategy mappingStrategy= new ColumnPositionMappingStrategy();
            mappingStrategy.setType(AvantLinkReturnsFeedData.class);

            //mappingStrategy.setColumnMapping(columns);

            writer.write(String.join(",", AvantLinkReturnsFeedData.columns)+"\n");

            // Creating StatefulBeanToCsv object
            StatefulBeanToCsvBuilder<AvantLinkReturnsFeedData> builder= new StatefulBeanToCsvBuilder(writer);
            StatefulBeanToCsv beanWriter = builder.withMappingStrategy(mappingStrategy).build();
            //StatefulBeanToCsv beanWriter = builder.build();

            // Write list to StatefulBeanToCsv object
            beanWriter.write(feed1DataList);

            // closing the writer object
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getStackTrace().toString());
        }
        return file;
    }

    public boolean upload(File file){
        logger.info("Avant link returns - uploading data...");

        //Ftp settings
        FTPSettings ftpSettings = FTPSettings.builder()
                .server(FTP_SERVER)
                .port(FTP_PORT)
                .username(FTP_USERNAME)
                .password(FTP_PASSWORD)
                .directory(FTP_DIRECTORY)
                .build();

        return ftpUpload.upload(ftpSettings, file);
        //--------------------//
    }

    public boolean move(File file) throws IOException {
        Path temp = Files.move
                (Paths.get(file.getPath()),
                        Paths.get(UPLOADED_CSV_FOLDER+"/"+file.getName()));

        if(temp != null)
        {
            logger.info("Avant link returns - file moved successfully....");
        }
        else
        {
            logger.log(Level.SEVERE, "Avant link returns - failed to move the file...");
            return false;
        }
        return true;
    }

    @Scheduled(cron="${cronjob.avantlink.retuns}")
    private void execute(){
        try {
            logger.info("Avant link returns - executing...");

            //1. ------- connect with multiple databases and fetch combined data ------//
            String customerId = "62b2101fac1c242045797d3e";//customer id
            CustomerOrder customerOrder = (CustomerOrder) fetch(customerId);

            //2. ---------- Convert data to feed format ------//
            List<AvantLinkReturnsFeedData> avantLinkSalesFeedDataList = (List<AvantLinkReturnsFeedData>) convert(customerOrder);

            //3. -------- Convert feed data to csv file -------//
            File file = save(avantLinkSalesFeedDataList);

            //4. -------- file uploading --------//
            boolean completed = upload(file);

            //5. --------- move file -----------//
            if(completed) move(file);

            logger.info("Avant link returns - execution completed...");
        }catch (Exception exception){
            logger.log(Level.SEVERE, exception.getStackTrace().toString());
        }
    }
}
