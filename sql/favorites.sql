CREATE TABLE `favorites` (
	`favoriteId` INT NOT NULL AUTO_INCREMENT,
	`userNo` INT NOT NULL,
	`placeName` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`address` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`createdAt` TIMESTAMP NULL DEFAULT (now()),
	PRIMARY KEY (`favoriteId`) USING BTREE,
	INDEX `userNo` (`userNo`) USING BTREE,
	CONSTRAINT `favorites_ibfk_1` FOREIGN KEY (`userNo`) REFERENCES `users` (`userNo`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COLLATE='utf8mb4_0900_ai_ci'
ENGINE=InnoDB
;
