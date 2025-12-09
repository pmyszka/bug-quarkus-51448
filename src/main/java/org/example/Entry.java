package org.example;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbImmutable(builder = Entry.Builder.class)
public class Entry {
  private final String pk;

  private Entry(Builder b) {
    this.pk = b.pk;
  }

  public static Builder builder() {
    return new Builder();
  }

  @DynamoDbPartitionKey
  public String pk() {
    return this.pk;
  }

  public static final class Builder {
    private String pk;

    private Builder() {}

    public Builder pk(String pk) {
      this.pk = pk;
      return this;
    }

    public Entry build() {
      throw new IllegalStateException();
    }
  }
}
