package org.example;

import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DdbUtils {

  static final String TABLE_NAME = "entries";
  private static final String PK = "pk";

  public static void setupDdbTable(DynamoDbAsyncClient client) {
    client
        .listTables()
        .thenCompose(
            response ->
                response.tableNames().isEmpty()
                    ? CompletableFuture.completedFuture(null)
                    : client.deleteTable(builder -> builder.tableName(TABLE_NAME)))
        .thenCompose(__ -> client.waiter().waitUntilTableNotExists(b -> b.tableName(TABLE_NAME)))
        .thenCompose(
            __ ->
                client.createTable(
                    builder ->
                        builder
                            .tableName(TABLE_NAME)
                            .keySchema(
                                KeySchemaElement.builder()
                                    .attributeName(PK)
                                    .keyType(KeyType.HASH)
                                    .build())
                            .attributeDefinitions(
                                AttributeDefinition.builder()
                                    .attributeName(PK)
                                    .attributeType(ScalarAttributeType.S)
                                    .build())
                            .provisionedThroughput(
                                pt -> pt.readCapacityUnits(300L).writeCapacityUnits(300L))))
        .thenCompose(__ -> client.waiter().waitUntilTableExists(b -> b.tableName(TABLE_NAME)))
        .toCompletableFuture()
        .join();

    client
        .putItem(
            builder -> builder.tableName(TABLE_NAME).item(Map.of(PK, AttributeValue.fromS("test"))))
        .toCompletableFuture()
        .join();
  }
}
