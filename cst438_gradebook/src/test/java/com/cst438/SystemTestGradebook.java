package com.cst438;
import org.apache.juli.logging.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLOutput;
import java.util.List;
import java.util.Objects;


import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class SystemTestGradebook {


    public static final String CHROME_DRIVER_FILE_LOCATION =
            "../../chromedriver-mac-arm64/chromedriver";
    public static final String URL = "http://localhost:3000";
//    public static final String ALIAS_NAME = "test";
    public static final int SLEEP_DURATION = 1000; // 1 second.

    WebDriver driver;

    @BeforeEach
    public void testSetup() throws Exception {
        // if you are not using Chrome,
        // the following lines will be different.
        System.setProperty(
                "webdriver.chrome.driver",
                CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(ops);


        driver.get(URL);
        // must have a short wait to allow time for the page to download
        Thread.sleep(SLEEP_DURATION);
    }

    @Test
    public void addAssignmentTest() {
        try {
            // AddAssignment xpath route
            WebElement addAssignmentButton = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/table/div/button"));

            // Check size of assignments
            List<WebElement> assignmentsBeforeInsertion = driver.findElements(By.xpath("//td"));

            // click addAssignment button
            addAssignmentButton.click();
            Thread.sleep(SLEEP_DURATION);

            // open react dialog
            WebElement addName = driver.findElement(By.xpath("//input[@name='assignmentName']"));
            WebElement addDueDate = driver.findElement(By.xpath("//input[@name='assignmentDueDate']"));
            WebElement addCourseID = driver.findElement(By.xpath("//input[@name='assignmentCourseID']"));
            WebElement submitNewAssignment = driver.findElement(By.id("sedit"));

            // add assignment name
            addName.sendKeys("Exam Review");
            Thread.sleep(SLEEP_DURATION);

            // add assignment date
            addDueDate.sendKeys("2023-01-01");
            Thread.sleep(SLEEP_DURATION);

            // add course_id
            addCourseID.sendKeys("40443");
            Thread.sleep(SLEEP_DURATION);

            // submit
            submitNewAssignment.click();
            Thread.sleep(SLEEP_DURATION);

            // Check size of assignments after insertion
            List<WebElement> assignments = driver.findElements(By.xpath("//td"));

            // Check that data was appended
            assertThat(assignments.size()).isGreaterThan(assignmentsBeforeInsertion.size());

            int assignmentNameIndex = assignmentsBeforeInsertion.size();
            int assignmentDueDateIndex = assignmentNameIndex + 2;
            String newAssignmentName = assignments.get(assignmentNameIndex).getText();
            String newAssignmentDueDate = assignments.get(assignmentDueDateIndex).getText();

            // Check that correct values were added
            assertEquals(newAssignmentName, "Exam Review");
            assertEquals(newAssignmentDueDate, "2023-01-01");

        } catch (InterruptedException e) {
            System.out.println("Add assignment test...");
            System.out.println("Catch exception when sleep fails...");
            System.out.println("Error caught: " + e);
        }
    }

    @Test
    public void editAssignmentTest() {
        try {
            // Check size of assignments
            List<WebElement> assignmentsBeforeEdit = driver.findElements(By.xpath("//td"));

            // Get the index of the edit button
            int editButtonIndex = assignmentsBeforeEdit.size() - 2;

            // Grab web element edit button
            WebElement editButton = assignmentsBeforeEdit.get(editButtonIndex);

            // open react dialog
            editButton.click();
            Thread.sleep(SLEEP_DURATION);

            // Elements in edit dialog
            WebElement editName = driver.findElement(By.xpath("//input[@name='assignmentName']"));
            WebElement editDueDate = driver.findElement(By.xpath("//input[@name='assignmentDueDate']"));

            // add assignment name

            // Web Element does not clear
            // https://stackoverflow.com/questions/50677760/selenium-clear-command-doesnt-clear-the-element
            // CONTROL and/or COMMAND are machine specific
            // editName.sendKeys(Keys.chord(Keys.COMMAND,"a"));
            // editName.sendKeys(Keys.chord(Keys.DELETE));
            // Thread.sleep(SLEEP_DURATION);

            // create something less machine specific
            int nameLength = editName.getAttribute("value").length();
            for(int i = 0; i < nameLength; i++){
                editName.sendKeys(Keys.BACK_SPACE);
            }
            Thread.sleep(SLEEP_DURATION);
            int newLength = editName.getAttribute("value").length();
            assertEquals(newLength, 0);

            String newName = "Exam Retake";
            String newDate = "2023-01-08";

            // Edit name
            editName.sendKeys(newName);
            Thread.sleep(SLEEP_DURATION);
            assertEquals(editName.getAttribute("value"), newName);
            assertEquals(editName.getAttribute("value").length(), newName.length());

            // Delete current Due Date
            int dueDateLength = editDueDate.getAttribute("value").length();
            for(int i = 0; i < dueDateLength; i++){
                editDueDate.sendKeys(Keys.BACK_SPACE);
            }
            Thread.sleep(SLEEP_DURATION);
            newLength = editDueDate.getAttribute("value").length();
            assertEquals(newLength, 0);

            // Edit due date
            editDueDate.sendKeys(newDate);
            Thread.sleep(SLEEP_DURATION);
            assertEquals(editDueDate.getAttribute("value"), newDate);
            assertEquals(editDueDate.getAttribute("value").length(), newDate.length());

            WebElement submitNewAssignment = driver.findElement(By.id("sedit"));

            // submit
            submitNewAssignment.click();
            Thread.sleep(SLEEP_DURATION);


            // Check size of assignments after insertion
            List<WebElement> assignments = driver.findElements(By.xpath("//td"));

            // Check that size has not changed
            assertEquals(assignmentsBeforeEdit.size(), assignments.size());

            int assignmentNameIndex = assignmentsBeforeEdit.size() - 6;
            int dueDateIndex = assignmentsBeforeEdit.size() - 4;
            String newAssignmentName = assignments.get(assignmentNameIndex).getText();
            String newAssignmentDueDate = assignments.get(dueDateIndex).getText();

            // Check that correct values were added
            assertEquals(newAssignmentName, "Exam Retake");
            assertEquals(newAssignmentDueDate, "2023-01-08");

        } catch (InterruptedException e) {
            System.out.println("Edit assignment test...");
            System.out.println("Catch exception when sleep fails...");
            System.out.println("Error caught: " + e);
        }
    }

    @Test
    public void deleteAssignmentTest() {
        try {
            // Check size of assignments
            List<WebElement> assignmentsBeforeDeletion = driver.findElements(By.xpath("//td"));

            // Get the index of the edit button
            int deleteButtonIndex = assignmentsBeforeDeletion.size() - 1;

            // Grab web element edit button
            WebElement deleteButton = assignmentsBeforeDeletion.get(deleteButtonIndex);

            // open react dialog
            deleteButton.click();
            Thread.sleep(SLEEP_DURATION);

            // how to check if alert exists
            try {
                // how to click on browser alert dialog
                // https://www.browserstack.com/guide/alerts-and-popups-in-selenium
                driver.switchTo().alert().accept();
            } catch (NoAlertPresentException e) {  // Check if the alert is present
                System.out.println();
            }

            Thread.sleep(SLEEP_DURATION);
        } catch (InterruptedException e) {
            System.out.println("Edit assignment test...");
            System.out.println("Catch exception when sleep fails...");
            System.out.println("Error caught: " + e);
        }
    }


    @AfterEach
    public void cleanup() {
        if (driver!=null) {
            driver.close();
            driver.quit();
            driver=null;
        }
    }
}
