resource "aws_dynamodb_table" "main" {
  name           = "${var.environment}-${var.table_name}"
  billing_mode   = var.billing_mode
  hash_key       = "PK"
  range_key      = "SK"
  stream_enabled = var.stream_enabled
  stream_view_type = "NEW_AND_OLD_IMAGES"

  attribute {
    name = "PK"
    type = "S"
  }

  attribute {
    name = "SK"
    type = "S"
  }

  # GSI for events by status
  global_secondary_index {
    name            = "GSI1"
    hash_key        = "GSI1PK"
    range_key       = "GSI1SK"
    projection_type = "ALL"
  }

  # GSI for events by organizer
  global_secondary_index {
    name            = "GSI2"
    hash_key        = "GSI2PK"
    range_key       = "GSI2SK"
    projection_type = "ALL"
  }

  # GSI for tickets by user
  global_secondary_index {
    name            = "GSI3"
    hash_key        = "GSI3PK"
    range_key       = "GSI3SK"
    projection_type = "ALL"
  }

  # GSI for tickets by event
  global_secondary_index {
    name            = "GSI4"
    hash_key        = "GSI4PK"
    range_key       = "GSI4SK"
    projection_type = "ALL"
  }

  tags = {
    Name        = "${var.environment}-${var.table_name}"
    Environment = var.environment
  }
}

# DynamoDB Stream Lambda Trigger
resource "aws_lambda_event_source_mapping" "dynamodb_stream" {
  count            = var.stream_enabled ? 1 : 0
  event_source_arn = aws_dynamodb_table.main.stream_arn
  function_name    = var.stream_lambda_function_name
  starting_position = "LATEST"
  batch_size       = 1
  enabled          = true
}

# CloudWatch Alarms for DynamoDB
resource "aws_cloudwatch_metric_alarm" "dynamodb_throttled_requests" {
  count               = var.enable_alarms ? 1 : 0
  alarm_name          = "${var.environment}-${var.table_name}-throttled-requests"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "ThrottledRequests"
  namespace           = "AWS/DynamoDB"
  period              = "300"
  statistic           = "Sum"
  threshold           = "10"
  alarm_description   = "DynamoDB throttled requests"
  alarm_actions       = var.alarm_actions

  dimensions = {
    TableName = aws_dynamodb_table.main.name
  }
}

resource "aws_cloudwatch_metric_alarm" "dynamodb_consumed_read_capacity" {
  count               = var.enable_alarms ? 1 : 0
  alarm_name          = "${var.environment}-${var.table_name}-consumed-read-capacity"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "ConsumedReadCapacityUnits"
  namespace           = "AWS/DynamoDB"
  period              = "300"
  statistic           = "Sum"
  threshold           = "1000"
  alarm_description   = "DynamoDB consumed read capacity"
  alarm_actions       = var.alarm_actions

  dimensions = {
    TableName = aws_dynamodb_table.main.name
  }
}

resource "aws_cloudwatch_metric_alarm" "dynamodb_consumed_write_capacity" {
  count               = var.enable_alarms ? 1 : 0
  alarm_name          = "${var.environment}-${var.table_name}-consumed-write-capacity"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "ConsumedWriteCapacityUnits"
  namespace           = "AWS/DynamoDB"
  period              = "300"
  statistic           = "Sum"
  threshold           = "1000"
  alarm_description   = "DynamoDB consumed write capacity"
  alarm_actions       = var.alarm_actions

  dimensions = {
    TableName = aws_dynamodb_table.main.name
  }
} 