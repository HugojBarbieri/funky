package com.funky.packageservice.service;

import com.funky.packageservice.client.FunkyClient;
import com.funky.packageservice.model.OrderDTO;
import com.funky.packageservice.model.PaymentStatus;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PackageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageService.class);

    private final XLSService xlsService;
    private final FunkyClient funkyClient;

    @Autowired
    public PackageService(XLSService xlsService, FunkyClient funkyClient) {
        this.xlsService = xlsService;
        this.funkyClient = funkyClient;
    }

    public List<OrderDTO> getUnpackagedAndPaidOrders() {
        return funkyClient.getUnpackagedOrders().stream()
                .filter(orderDTO -> PaymentStatus.PAID.getName()
                        .equals(orderDTO.getPaymentStatus())).collect(Collectors.toList());
    }

    public List<OrderDTO> getUnpackagedOrders() {
        return funkyClient.getUnpackagedOrders();
    }

    public Workbook getWorkbook() {
        LOGGER.info("Starting to get workbook from service");
        return xlsService.getWorkbookFromOrders(getUnpackagedAndPaidOrders());
    }

    public String fileName() {
        LOGGER.info("Starting to getting filename from service");
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd_MMM");
        String dateFileName = currentDate.format(formatter);
        LOGGER.info(String.format("File name is: %s", dateFileName));
        return String.format("orders_%s.xlsx", dateFileName);
    }
}
