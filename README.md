# Event Registration System

A scalable, cloud-native, event-driven platform built to handle high-concurrency event registrations and real-time ticket validation using AWS services.

## 🏗️ Architecture

The system follows a serverless, event-driven architecture using AWS services:

- **Frontend**: React with Material-UI
- **Backend**: Spring Boot with AWS integrations
- **Database**: DynamoDB with streams for real-time updates
- **Compute**: AWS Lambda for serverless processing
- **Messaging**: SQS/SNS for notifications
- **Infrastructure**: Terraform for IaC
- **CI/CD**: GitHub Actions for automation

## 🚀 Key Features

- **User Authentication**: Secure JWT-based authentication with role-based access
- **Event Management**: Create, update, browse, and search events
- **Ticket System**: Real-time ticket availability and QR code generation
- **Real-time Validation**: Ticket validation using DynamoDB Streams and Lambda
- **Notifications**: Email notifications for registrations and updates
- **Admin Dashboard**: Real-time analytics and event management
- **Scalable Infrastructure**: Supports 10,000+ concurrent users

## 📁 Project Structure

```
EventRegistrationSystem/
├── frontend/                 # React application
├── backend/                  # Spring Boot application
├── infrastructure/           # Terraform configurations
├── .github/                  # GitHub Actions workflows
└── docs/                     # Documentation
```

## 🛠️ Tech Stack

### Frontend
- React 18
- Material-UI (MUI)
- Recharts for analytics
- Axios for API calls
- React Router for navigation

### Backend
- Spring Boot 3.x
- Spring Security with JWT
- AWS SDK for Java
- DynamoDB integration
- SQS/SNS messaging

### Infrastructure
- Terraform
- AWS Lambda
- DynamoDB with streams
- API Gateway
- CloudFront
- CloudWatch

## 🚀 Quick Start

### Prerequisites
- Node.js 18+
- Java 17+
- AWS CLI configured
- Terraform installed

### Frontend Setup
```bash
cd frontend
npm install
npm start
```

### Backend Setup
```bash
cd backend
./mvnw spring-boot:run
```

### Infrastructure Deployment
```bash
cd infrastructure
terraform init
terraform plan
terraform apply
```

## 📊 System Capabilities

- **Scalability**: Handles 10,000+ concurrent users
- **Real-time Updates**: DynamoDB Streams for live data
- **Fault Tolerance**: Multi-AZ deployment with auto-scaling
- **Monitoring**: CloudWatch integration for performance tracking
- **Security**: JWT authentication, encrypted data transmission

## 🔧 Development

### Environment Variables
Create `.env` files in both frontend and backend directories with:
- AWS credentials
- Database connection strings
- JWT secrets
- API endpoints

### Testing
```bash
# Frontend tests
cd frontend && npm test

# Backend tests
cd backend && ./mvnw test
```

## 📈 Monitoring

- CloudWatch dashboards for system metrics
- Real-time error tracking
- Performance monitoring
- Cost optimization insights

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.