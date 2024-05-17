package com.funky.packageservice.controller;

import com.funky.packageservice.PackageService;
import com.funky.packageservice.client.FunkyClient;
import com.funky.packageservice.model.OrderDTO;
import com.funky.packageservice.model.ProductDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/package")
public class PackageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageController.class);

    private CellStyle greyCellStyle;
    private CellStyle cellStyle;

    private final PackageService packageService;

    @Autowired
    public PackageController(PackageService packageService) {
        this.packageService = packageService;
    }

    @GetMapping("/download-excel")
    public ResponseEntity<byte[]> downloadExcel() {
        try {
            Workbook workbook = packageService.getWorkbook();

            // Convert the workbook to a byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] excelBytes = outputStream.toByteArray();

            // Set headers for the response
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders.xlsx");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            LOGGER.error("Failed to create the Excel file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/save-excel")
    public ResponseEntity<String> saveExcel() {
        try {
            Workbook workbook = packageService.getWorkbook();
            // Save the workbook to a file
            FileOutputStream fileOut = new FileOutputStream("orders.xlsx");
            workbook.write(fileOut);
            fileOut.close();

            return ResponseEntity.ok("Excel file saved successfully in path");
        } catch (IOException e) {
            LOGGER.error("Failed to send create the excel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save Excel file.");
        }
    }

    private void writeDataToSheet(List<OrderDTO> orders, Sheet sheet, Workbook workbook) {
        // Create font with larger size
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 8); // Adjust font size as needed

        // Create cell style with the larger font
        cellStyle = getCellStyle(workbook, font);

        // Create a grey cell style with light grey background
        greyCellStyle = getCellStyle(workbook, cellStyle);

        // Set print properties
        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setPaperSize(PrintSetup.A4_PAPERSIZE);
        printSetup.setLandscape(true); // Vertical orientation
        int rowIndex = 0;

        for (OrderDTO orderDTO : orders) {
            // Write order details and customer name
            writeOrderDetails(sheet, orderDTO, rowIndex, cellStyle, greyCellStyle);
            rowIndex += 2; // Move to the next row after writing order details and adding an empty row
            writeOrderNotesDetails(sheet, orderDTO, rowIndex, cellStyle, greyCellStyle);
            rowIndex += 2;
            // Write column names for products
            writeProductHeader(sheet, rowIndex, cellStyle);
            rowIndex++;

            // Write product data
            rowIndex = writeProductData(sheet, orderDTO.getProducts(), rowIndex);

            // Add empty row between orders
            rowIndex++;
            totalQuantityProducts(sheet, orderDTO.getProducts().size(), rowIndex, cellStyle);
            rowIndex++;
            rowIndex++;

            // Write a row of dashes between orders
            writeDashRow(sheet, rowIndex, cellStyle);
            rowIndex++;

        }

        // Autosize columns after data is written
        for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static CellStyle getCellStyle(Workbook workbook, Font font) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(font);
        return cellStyle;
    }

    private static CellStyle getCellStyle(Workbook workbook, CellStyle cellStyle) {
        CellStyle greyCellStyle = workbook.createCellStyle();
        greyCellStyle.cloneStyleFrom(cellStyle); // Copy the style from the default cell style
        greyCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        greyCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        return greyCellStyle;
    }

    private void writeOrderNotesDetails(Sheet sheet, OrderDTO orderDTO, int rowIndex, CellStyle cellStyle, CellStyle greyCellStyle) {
        Row orderRow = sheet.createRow(rowIndex);
        writeCell(orderRow, 0, "Notas clientes:", greyCellStyle);
        writeCell(orderRow, 1, orderDTO.getNote(), cellStyle);
        writeCell(orderRow, 2, "Notas Nuestras:", greyCellStyle);
        writeCell(orderRow, 3, orderDTO.getOwnerNote(), greyCellStyle);
    }

    private void writeOrderDetails(Sheet sheet, OrderDTO orderDTO, int rowIndex, CellStyle cellStyle, CellStyle greyCellStyle) {
        Row orderRow = sheet.createRow(rowIndex);
        writeCell(orderRow, 0, "Orden NRO:", greyCellStyle);
        writeCell(orderRow, 1, orderDTO.getNumber() + "", greyCellStyle);
        writeCell(orderRow, 2, "Nombre:", greyCellStyle);
        writeCell(orderRow, 3, orderDTO.getCustomer().getName(), greyCellStyle);
        writeCell(orderRow, 4, "PAGADA:", greyCellStyle);
        if (orderDTO.getPaymentStatus().equals("pending")) {
            writeCell(orderRow, 5, "SI", greyCellStyle);
        } else {
            writeCell(orderRow, 5, "NO", cellStyle);
        }

        //here is when not paid yet
    }

    private void totalQuantityProducts(Sheet sheet, Integer quantityProducts, int rowIndex, CellStyle cellStyle) {
        Row productHeaderRow = sheet.createRow(rowIndex);
        writeCell(productHeaderRow, 0, "Cantidad Articulos", cellStyle);
        writeCell(productHeaderRow, 1, quantityProducts + "", cellStyle);
    }
    private void writeProductHeader(Sheet sheet, int rowIndex, CellStyle cellStyle) {
        Row productHeaderRow = sheet.createRow(rowIndex);
        writeCell(productHeaderRow, 0, "Producto Nombre", cellStyle);
        writeCell(productHeaderRow, 1, "Cantidad", cellStyle);
    }

    private int writeProductData(Sheet sheet, List<ProductDTO> products, int rowIndex) {
        boolean switchFont = true;
        for (ProductDTO product : products) {
            CellStyle cellStyle = switchFont ? greyCellStyle : this.cellStyle;
            Row productRow = sheet.createRow(rowIndex++);
            writeCell(productRow, 0, product.getName(), cellStyle);
            writeCell(productRow, 1, product.getQuantity() + "", cellStyle);
            switchFont = !switchFont;
        }
        return rowIndex;
    }

    private void writeCell(Row row, int column, String value, CellStyle cellStyle) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(cellStyle);
    }

    private void writeDashRow(Sheet sheet, int rowIndex, CellStyle cellStyle) {
        Row dashRow = sheet.createRow(rowIndex);
        writeCell(dashRow, 0, "#".repeat(35), cellStyle);
    }

}
