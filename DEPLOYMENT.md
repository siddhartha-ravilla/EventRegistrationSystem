# Deployment Guide

This guide provides step-by-step instructions for deploying the Event Registration System to different environments.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [AWS Deployment](#aws-deployment)
4. [Docker Deployment](#docker-deployment)
5. [Production Checklist](#production-checklist)
6. [Monitoring and Maintenance](#monitoring-and-maintenance)

## Prerequisites

### Required Tools
- Java 17 or later
- Node.js 18 or later
- Docker (for containerized deployment)
- AWS CLI (for AWS deployment)
- Terraform (for infrastructure deployment)

### AWS Services Required
- AWS Account with appropriate permissions
- DynamoDB
- Lambda
- API Gateway
- SQS
- SNS
- SES
- CloudFront
- S3
- CloudWatch
- IAM

## Local Development Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd EventRegistrationSystem
```

### 2. Run the Setup Script
```bash
chmod +x setup.sh
./setup.sh
```

### 3. Manual Setup (Alternative)

#### Backend Setup
```bash
cd backend
./mvnw spring-boot:run
```

#### Frontend Setup
```bash
cd frontend
npm install
npm start
```

### 4. Access the Application
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api
- H2 Database Console: http://localhost:8080/h2-console

## AWS Deployment

### 1. Configure AWS Credentials
```bash
aws configure
```

### 2. Create S3 Bucket for Terraform State
```bash
aws s3 mb s3://event-registration-terraform-state
aws s3api put-bucket-versioning --bucket event-registration-terraform-state --versioning-configuration Status=Enabled
```

### 3. Deploy Infrastructure
```bash
cd infrastructure
terraform init
terraform plan
terraform apply
```

### 4. Configure Environment Variables

#### Backend Environment Variables
```bash
# Create .env file in backend directory
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
JWT_SECRET=your-secure-jwt-secret
SQS_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/your-queue-url
SNS_TOPIC_ARN=arn:aws:sns:us-east-1:your-topic-arn
SES_FROM_EMAIL=noreply@yourdomain.com
```

#### Frontend Environment Variables
```bash
# Create .env file in frontend directory
REACT_APP_API_URL=https://your-api-gateway-url.amazonaws.com/prod/api
REACT_APP_ENVIRONMENT=production
```

### 5. Deploy Backend to AWS

#### Using Docker
```bash
cd backend
docker build -t event-registration-backend .
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin your-account-id.dkr.ecr.us-east-1.amazonaws.com
docker tag event-registration-backend:latest your-account-id.dkr.ecr.us-east-1.amazonaws.com/event-registration-backend:latest
docker push your-account-id.dkr.ecr.us-east-1.amazonaws.com/event-registration-backend:latest
```

#### Using AWS Lambda
```bash
cd backend
./mvnw clean package
aws lambda update-function-code --function-name event-registration-backend --zip-file fileb://target/event-registration-backend-1.0.0.jar
```

### 6. Deploy Frontend to S3/CloudFront
```bash
cd frontend
npm run build
aws s3 sync build/ s3://your-frontend-bucket --delete
aws cloudfront create-invalidation --distribution-id your-distribution-id --paths "/*"
```

## Docker Deployment

### 1. Build Docker Images
```bash
# Build backend image
cd backend
docker build -t event-registration-backend .

# Build frontend image
cd ../frontend
docker build -t event-registration-frontend .
```

### 2. Create Docker Compose File
```yaml
# docker-compose.yml
version: '3.8'
services:
  backend:
    image: event-registration-backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - AWS_REGION=us-east-1
    depends_on:
      - postgres
    networks:
      - app-network

  frontend:
    image: event-registration-frontend
    ports:
      - "3000:3000"
    environment:
      - REACT_APP_API_URL=http://localhost:8080/api
    depends_on:
      - backend
    networks:
      - app-network

  postgres:
    image: postgres:13
    environment:
      POSTGRES_DB: event_registration
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - app-network

volumes:
  postgres_data:

networks:
  app-network:
    driver: bridge
```

### 3. Run with Docker Compose
```bash
docker-compose up -d
```

## Production Checklist

### Security
- [ ] JWT secret is strong and unique
- [ ] AWS credentials are properly configured
- [ ] HTTPS is enabled for all endpoints
- [ ] CORS is properly configured
- [ ] Input validation is implemented
- [ ] SQL injection protection is in place
- [ ] XSS protection is enabled

### Performance
- [ ] Database indexes are optimized
- [ ] CDN is configured for static assets
- [ ] Caching is implemented
- [ ] Load balancing is configured
- [ ] Auto-scaling is enabled

### Monitoring
- [ ] CloudWatch alarms are configured
- [ ] Application logging is implemented
- [ ] Error tracking is set up
- [ ] Performance monitoring is active
- [ ] Health checks are configured

### Backup and Recovery
- [ ] Database backups are automated
- [ ] Disaster recovery plan is documented
- [ ] Data retention policies are defined
- [ ] Backup testing is scheduled

## Monitoring and Maintenance

### CloudWatch Dashboards
Create custom dashboards for:
- API Gateway metrics
- Lambda function performance
- DynamoDB read/write capacity
- SQS queue depth
- Error rates and response times

### Log Management
- Configure CloudWatch Logs for Lambda functions
- Set up log retention policies
- Implement log aggregation
- Configure log-based alerts

### Performance Optimization
- Monitor DynamoDB throttling
- Optimize Lambda cold starts
- Review API Gateway caching
- Analyze CloudFront hit rates

### Security Monitoring
- Set up AWS GuardDuty
- Monitor IAM access
- Review CloudTrail logs
- Implement security scanning

### Regular Maintenance
- Update dependencies monthly
- Review and rotate secrets
- Monitor AWS service quotas
- Perform security audits
- Update SSL certificates

## Troubleshooting

### Common Issues

#### Backend Issues
1. **Database Connection Errors**
   - Check AWS credentials
   - Verify DynamoDB table exists
   - Check IAM permissions

2. **Lambda Timeout Errors**
   - Increase timeout settings
   - Optimize code performance
   - Check memory allocation

3. **JWT Token Issues**
   - Verify JWT secret configuration
   - Check token expiration settings
   - Validate token format

#### Frontend Issues
1. **API Connection Errors**
   - Verify API Gateway URL
   - Check CORS configuration
   - Validate authentication headers

2. **Build Failures**
   - Check Node.js version
   - Clear npm cache
   - Update dependencies

#### Infrastructure Issues
1. **Terraform Deployment Failures**
   - Check AWS credentials
   - Verify resource limits
   - Review error logs

2. **CloudFormation Stack Errors**
   - Check resource dependencies
   - Verify IAM permissions
   - Review stack events

### Support and Resources
- AWS Documentation: https://docs.aws.amazon.com/
- Terraform Documentation: https://www.terraform.io/docs
- Spring Boot Documentation: https://spring.io/projects/spring-boot
- React Documentation: https://reactjs.org/docs/

## Cost Optimization

### AWS Cost Management
- Use AWS Cost Explorer to monitor spending
- Set up billing alerts
- Implement auto-scaling policies
- Use reserved instances for predictable workloads
- Optimize DynamoDB read/write capacity

### Resource Optimization
- Implement caching strategies
- Use CloudFront for content delivery
- Optimize Lambda function memory
- Review and remove unused resources
- Use S3 lifecycle policies

## Conclusion

This deployment guide provides a comprehensive approach to deploying the Event Registration System. Follow the checklist and best practices to ensure a secure, scalable, and maintainable deployment.

For additional support or questions, please refer to the project documentation or create an issue in the repository. 