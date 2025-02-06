CREATE TABLE `survey_results` (
    `resultNo` INT NOT NULL AUTO_INCREMENT,
    `userNo` INT NOT NULL,
    `place_name` VARCHAR(255) NOT NULL,
    `address` VARCHAR(255) NOT NULL,
    `createdAt` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`resultNo`),
    FOREIGN KEY (`userNo`) REFERENCES users(`userNo`),
    INDEX `idx_userNo` (`userNo`)
) 
COLLATE='utf8mb4_0900_ai_ci'
ENGINE=InnoDB;