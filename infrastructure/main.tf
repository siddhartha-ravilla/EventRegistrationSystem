terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  
  backend "s3" {
    bucket = "event-registration-terraform-state"
    key    = "terraform.tfstate"
    region = "us-east-1"
  }
}

provider "aws" {
  region = var.aws_region
  
  default_tags {
    tags = {
      Project     = "EventRegistrationSystem"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}

# VPC and Networking
module "vpc" {
  source = "./modules/vpc"
  
  environment = var.environment
  vpc_cidr    = var.vpc_cidr
  azs         = var.availability_zones
}

# DynamoDB
module "dynamodb" {
  source = "./modules/dynamodb"
  
  environment = var.environment
  table_name  = "event-registration"
}

# Lambda Functions
module "lambda" {
  source = "./modules/lambda"
  
  environment = var.environment
  vpc_id      = module.vpc.vpc_id
  subnet_ids  = module.vpc.private_subnet_ids
}

# SQS Queues
module "sqs" {
  source = "./modules/sqs"
  
  environment = var.environment
}

# SNS Topics
module "sns" {
  source = "./modules/sns"
  
  environment = var.environment
}

# API Gateway
module "api_gateway" {
  source = "./modules/api_gateway"
  
  environment = var.environment
  lambda_functions = module.lambda.function_arns
}

# CloudFront
module "cloudfront" {
  source = "./modules/cloudfront"
  
  environment = var.environment
  domain_name = var.domain_name
}

# CloudWatch
module "cloudwatch" {
  source = "./modules/cloudwatch"
  
  environment = var.environment
  lambda_functions = module.lambda.function_names
}

# IAM Roles and Policies
module "iam" {
  source = "./modules/iam"
  
  environment = var.environment
  lambda_role_arn = module.lambda.role_arn
  dynamodb_table_arn = module.dynamodb.table_arn
  sqs_queue_arns = module.sqs.queue_arns
  sns_topic_arns = module.sns.topic_arns
} 