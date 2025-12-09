package org.example;

import static org.example.DdbUtils.TABLE_NAME;
import static org.example.DdbUtils.setupDdbTable;
import static software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional.keyEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.inject.Inject;
import mutiny.zero.flow.adapters.AdaptersToFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Testcontainers
public class IllegalStateExceptionTestNonQuarkus {

  @Container
  private static final LocalStackContainer LOCALSTACK_CONTAINER =
      new LocalStackContainer(DockerImageName.parse("localstack/localstack:4.1"))
          .withServices(LocalStackContainer.Service.DYNAMODB);

  private DynamoDbTable<Entry> table;
  private DynamoDbAsyncTable<Entry> tableAsync;

  private DynamoDbClient ddbClient;
  private DynamoDbAsyncClient ddbAsyncClient;

  @BeforeEach
  void setup() {
    ddbClient =
        DynamoDbClient.builder()
            .endpointOverride(
                LOCALSTACK_CONTAINER.getEndpointOverride(LocalStackContainer.Service.DYNAMODB))
            .build();
    ddbAsyncClient =
        DynamoDbAsyncClient.builder()
            .endpointOverride(
                LOCALSTACK_CONTAINER.getEndpointOverride(LocalStackContainer.Service.DYNAMODB))
            .build();

    setupDdbTable(ddbAsyncClient);

    table =
        DynamoDbEnhancedClient.builder()
            .dynamoDbClient(ddbClient)
            .build()
            .table(TABLE_NAME, TableSchema.fromImmutableClass(Entry.class));

    tableAsync =
        DynamoDbEnhancedAsyncClient.builder()
            .dynamoDbClient(ddbAsyncClient)
            .build()
            .table(TABLE_NAME, TableSchema.fromImmutableClass(Entry.class));
  }

  @Test
  public void retrieve() {
    Multi.createFrom()
        .iterable(table.query(keyEqualTo(k -> k.partitionValue("test"))).items())
        .collect()
        .asList()
        .subscribe()
        .withSubscriber(UniAssertSubscriber.create())
        .awaitFailure(Throwable::printStackTrace);
  }

  @Test
  public void retrieveAsync() {
    Multi.createFrom()
        .publisher(
            AdaptersToFlow.publisher(
                tableAsync.query(keyEqualTo(k -> k.partitionValue("test"))).items()))
        .collect()
        .asList()
        .subscribe()
        .withSubscriber(UniAssertSubscriber.create())
        .awaitFailure(Throwable::printStackTrace);
  }
}
