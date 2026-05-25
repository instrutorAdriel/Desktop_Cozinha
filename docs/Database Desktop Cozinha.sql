CREATE DATABASE  IF NOT EXISTS `desktop_cozinha` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `desktop_cozinha`;
-- MySQL dump 10.13  Distrib 8.0.33, for Win64 (x86_64)
--
-- Host: localhost    Database: desktop_cozinha
-- ------------------------------------------------------
-- Server version	8.0.34

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `estoque_geral`
--

DROP TABLE IF EXISTS `estoque_geral`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `estoque_geral` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `nome` varchar(100) NOT NULL,
  `tipo` enum('PERECIVEL','NAO_PERECIVEL','UTENSILIO') NOT NULL,
  `quantidade_atual` decimal(10,3) DEFAULT NULL,
  `unidade_medida` enum('KG','LITRO','UNIDADE') NOT NULL,
  `estoque_minimo` decimal(10,3) DEFAULT NULL,
  `data_validade` date DEFAULT NULL,
  `data_cadastro` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `estoque_geral`
--

LOCK TABLES `estoque_geral` WRITE;
/*!40000 ALTER TABLE `estoque_geral` DISABLE KEYS */;
INSERT INTO `estoque_geral` VALUES (1,'Queijo Mozarela','PERECIVEL',14.200,'KG',5.000,'2026-06-10',NULL),(2,'Presunto Cozido','PERECIVEL',3.100,'KG',4.000,'2026-05-28',NULL),(3,'Creme de Leite Fresco','PERECIVEL',18.000,'LITRO',6.000,'2026-06-02',NULL),(4,'Manteiga com Sal','PERECIVEL',22.500,'KG',8.000,'2026-08-14',NULL),(5,'Ovos Brancos Tipo Grande','PERECIVEL',180.000,'UNIDADE',60.000,'2026-06-01',NULL),(6,'Alface Crespa Hidropônica','PERECIVEL',15.000,'UNIDADE',20.000,'2026-05-18',NULL),(7,'Cebola Perola','PERECIVEL',45.000,'KG',15.000,'2026-06-15',NULL),(8,'Batata Monalisa','PERECIVEL',12.000,'KG',25.000,'2026-05-25',NULL),(9,'Carne Moída Patinho','PERECIVEL',28.400,'KG',10.000,'2026-05-20',NULL),(10,'Filé de Salmão Fresco','PERECIVEL',0.000,'KG',6.000,'2026-05-16',NULL),(11,'Suco de Laranja Integral','PERECIVEL',35.000,'LITRO',12.000,'2026-05-30',NULL),(12,'Bacon em Cubos','PERECIVEL',11.800,'KG',5.000,'2026-07-19',NULL),(13,'Açúcar Refinado','NAO_PERECIVEL',60.000,'KG',20.000,NULL,NULL),(14,'Sal Refinado Iodado','NAO_PERECIVEL',15.000,'KG',5.000,NULL,NULL),(15,'Café Torrado e Moído','NAO_PERECIVEL',40.000,'KG',10.000,NULL,NULL),(16,'Macarrão Espaguete n8','NAO_PERECIVEL',95.000,'KG',25.000,NULL,NULL),(17,'Extrato de Tomate','NAO_PERECIVEL',54.000,'UNIDADE',15.000,NULL,NULL),(18,'Farinha de Trigo Tipo 1','NAO_PERECIVEL',110.000,'KG',30.000,NULL,NULL),(19,'Milho em Conserva','NAO_PERECIVEL',8.000,'UNIDADE',24.000,NULL,NULL),(20,'Ervilha em Conserva','NAO_PERECIVEL',32.000,'UNIDADE',15.000,NULL,NULL),(21,'Vinagre de Álcool','NAO_PERECIVEL',14.000,'LITRO',5.000,NULL,NULL),(22,'Azeite de Oliva Extra Virgem','NAO_PERECIVEL',28.000,'LITRO',8.000,NULL,NULL),(23,'Sardinha em Lata','NAO_PERECIVEL',48.000,'UNIDADE',12.000,NULL,NULL),(24,'Atum Ralado em Óleo','NAO_PERECIVEL',36.000,'UNIDADE',12.000,NULL,NULL),(25,'Maionese Balde 3kg','NAO_PERECIVEL',5.000,'UNIDADE',3.000,NULL,NULL),(26,'Ketchup Balde 3.5kg','NAO_PERECIVEL',2.000,'UNIDADE',4.000,NULL,NULL),(27,'Mostarda Balde 3kg','NAO_PERECIVEL',4.000,'UNIDADE',2.000,NULL,NULL),(28,'Fermento Químico em Pó','NAO_PERECIVEL',12.000,'UNIDADE',4.000,NULL,NULL),(29,'Frigideira Antiaderente 24cm','UTENSILIO',8.000,'UNIDADE',2.000,NULL,NULL),(30,'Colher de Silicone','UTENSILIO',15.000,'UNIDADE',5.000,NULL,NULL),(31,'Concha de Inox','UTENSILIO',6.000,'UNIDADE',2.000,NULL,NULL),(32,'Escumadeira de Inox','UTENSILIO',5.000,'UNIDADE',2.000,NULL,NULL),(33,'Pegador de Massa Inox','UTENSILIO',1.000,'UNIDADE',3.000,NULL,NULL),(34,'Ralarador de Legumes Inox','UTENSILIO',4.000,'UNIDADE',2.000,NULL,NULL),(35,'Abridor de Latas Industrial','UTENSILIO',2.000,'UNIDADE',1.000,NULL,NULL),(36,'Fuê / Batedor de Claras','UTENSILIO',7.000,'UNIDADE',2.000,NULL,NULL),(37,'Forma de Pizza Alumínio 35cm','UTENSILIO',20.000,'UNIDADE',10.000,NULL,NULL),(38,'Assadeira Retangular Alta','UTENSILIO',14.000,'UNIDADE',5.000,NULL,NULL),(39,'Bowl de Inox 4 Litros','UTENSILIO',10.000,'UNIDADE',4.000,NULL,NULL),(40,'Copo Medidor Plástico 1L','UTENSILIO',3.000,'UNIDADE',3.000,NULL,NULL);
/*!40000 ALTER TABLE `estoque_geral` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `historico_movimentacao`
--

DROP TABLE IF EXISTS `historico_movimentacao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `historico_movimentacao` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `quantidade` int DEFAULT NULL,
  `data_hora` timestamp NULL DEFAULT NULL,
  `produto_id` int unsigned DEFAULT NULL,
  `usuario_id` int unsigned DEFAULT NULL,
  `tipo_estoque` enum('PERECIVEL','NAO_PERECIVEL','UTENSILIO') NOT NULL,
  `tipo_movimentacao` enum('ENTRADA','SAIDA','DESCARTE') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `produto_id` (`produto_id`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `historico_movimentacao_ibfk_1` FOREIGN KEY (`produto_id`) REFERENCES `estoque_geral` (`id`),
  CONSTRAINT `historico_movimentacao_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `historico_movimentacao`
--

LOCK TABLES `historico_movimentacao` WRITE;
/*!40000 ALTER TABLE `historico_movimentacao` DISABLE KEYS */;
INSERT INTO `historico_movimentacao` VALUES (1,50,'2026-05-15 11:00:00',1,1,'PERECIVEL','ENTRADA'),(2,10,'2026-05-15 12:30:00',2,1,'NAO_PERECIVEL','ENTRADA'),(3,5,'2026-05-15 13:15:00',3,1,'UTENSILIO','ENTRADA'),(4,20,'2026-05-16 17:00:00',4,1,'PERECIVEL','ENTRADA'),(5,15,'2026-05-16 19:45:00',1,1,'PERECIVEL','SAIDA'),(6,30,'2026-05-17 10:10:00',5,1,'NAO_PERECIVEL','ENTRADA'),(7,2,'2026-05-17 14:20:00',6,1,'UTENSILIO','ENTRADA'),(8,40,'2026-05-18 11:30:00',7,1,'PERECIVEL','ENTRADA'),(9,12,'2026-05-18 16:15:00',8,1,'NAO_PERECIVEL','ENTRADA'),(10,8,'2026-05-19 12:00:00',9,1,'PERECIVEL','ENTRADA'),(11,25,'2026-05-19 18:30:00',10,1,'NAO_PERECIVEL','ENTRADA'),(12,3,'2026-05-20 13:00:00',11,1,'UTENSILIO','ENTRADA'),(13,4,'2026-05-20 19:22:00',2,1,'NAO_PERECIVEL','SAIDA'),(14,10,'2026-05-21 11:05:00',7,1,'PERECIVEL','SAIDA'),(15,1,'2026-05-21 12:40:00',3,1,'UTENSILIO','SAIDA');
/*!40000 ALTER TABLE `historico_movimentacao` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `nome` varchar(100) NOT NULL,
  `email` varchar(255) NOT NULL,
  `senha` varchar(255) NOT NULL,
  `token` varchar(2500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (1,'GERENTE','ADMIN','$2a$10$oadcPGDcpj3OdErNeOoiHOGNcEK4IVIabPzyx.NJZnscL60lqJ3ZG',NULL);
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-21 11:49:10
