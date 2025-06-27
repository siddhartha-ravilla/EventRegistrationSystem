#!/bin/bash

# Event Registration System Setup Script
# This script helps you set up the development environment

set -e

echo "🚀 Setting up Event Registration System..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if required tools are installed
check_requirements() {
    print_status "Checking system requirements..."
    
    # Check Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed. Please install Java 17 or later."
        exit 1
    fi
    
    java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$java_version" -lt 17 ]; then
        print_error "Java 17 or later is required. Current version: $java_version"
        exit 1
    fi
    print_success "Java version: $(java -version 2>&1 | head -n 1)"
    
    # Check Node.js
    if ! command -v node &> /dev/null; then
        print_error "Node.js is not installed. Please install Node.js 18 or later."
        exit 1
    fi
    
    node_version=$(node --version | cut -d'v' -f2 | cut -d'.' -f1)
    if [ "$node_version" -lt 18 ]; then
        print_error "Node.js 18 or later is required. Current version: $node_version"
        exit 1
    fi
    print_success "Node.js version: $(node --version)"
    
    # Check npm
    if ! command -v npm &> /dev/null; then
        print_error "npm is not installed."
        exit 1
    fi
    print_success "npm version: $(npm --version)"
    
    # Check Docker (optional)
    if command -v docker &> /dev/null; then
        print_success "Docker is available"
    else
        print_warning "Docker is not installed. It's required for containerized deployment."
    fi
    
    # Check AWS CLI (optional)
    if command -v aws &> /dev/null; then
        print_success "AWS CLI is available"
    else
        print_warning "AWS CLI is not installed. It's required for AWS deployment."
    fi
    
    # Check Terraform (optional)
    if command -v terraform &> /dev/null; then
        print_success "Terraform is available"
    else
        print_warning "Terraform is not installed. It's required for infrastructure deployment."
    fi
}

# Setup backend
setup_backend() {
    print_status "Setting up Spring Boot backend..."
    
    cd backend
    
    # Check if Maven wrapper exists
    if [ ! -f "./mvnw" ]; then
        print_status "Downloading Maven wrapper..."
        mvn wrapper:wrapper
    fi
    
    # Install dependencies
    print_status "Installing Maven dependencies..."
    ./mvnw dependency:resolve
    
    # Build the project
    print_status "Building Spring Boot application..."
    ./mvnw clean compile
    
    print_success "Backend setup completed!"
    cd ..
}

# Setup frontend
setup_frontend() {
    print_status "Setting up React frontend..."
    
    cd frontend
    
    # Install dependencies
    print_status "Installing npm dependencies..."
    npm install
    
    # Create .env file if it doesn't exist
    if [ ! -f ".env" ]; then
        print_status "Creating .env file..."
        cat > .env << EOF
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_ENVIRONMENT=development
EOF
        print_success "Created .env file"
    fi
    
    print_success "Frontend setup completed!"
    cd ..
}

# Create environment files
create_env_files() {
    print_status "Creating environment configuration files..."
    
    # Backend .env
    if [ ! -f "backend/.env" ]; then
        cat > backend/.env << EOF
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=password

# JWT Configuration
JWT_SECRET=your-secret-key-here-make-it-long-and-secure
JWT_EXPIRATION=86400000

# AWS Configuration (for production)
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key

# DynamoDB Configuration
AWS_DYNAMODB_TABLE_NAME=event-registration
AWS_DYNAMODB_STREAM_ENABLED=true

# SQS Configuration
AWS_SQS_QUEUE_URL=your-sqs-queue-url

# SNS Configuration
AWS_SNS_TOPIC_ARN=your-sns-topic-arn

# SES Configuration
AWS_SES_FROM_EMAIL=noreply@eventregistration.com
EOF
        print_success "Created backend .env file"
    fi
    
    # Frontend .env
    if [ ! -f "frontend/.env" ]; then
        cat > frontend/.env << EOF
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_ENVIRONMENT=development
REACT_APP_VERSION=1.0.0
EOF
        print_success "Created frontend .env file"
    fi
}

# Run tests
run_tests() {
    print_status "Running tests..."
    
    # Backend tests
    print_status "Running backend tests..."
    cd backend
    ./mvnw test
    cd ..
    
    # Frontend tests
    print_status "Running frontend tests..."
    cd frontend
    npm test -- --watchAll=false --passWithNoTests
    cd ..
    
    print_success "All tests completed!"
}

# Start development servers
start_dev_servers() {
    print_status "Starting development servers..."
    
    # Start backend in background
    print_status "Starting Spring Boot backend on http://localhost:8080"
    cd backend
    ./mvnw spring-boot:run &
    BACKEND_PID=$!
    cd ..
    
    # Wait for backend to start
    sleep 10
    
    # Start frontend
    print_status "Starting React frontend on http://localhost:3000"
    cd frontend
    npm start &
    FRONTEND_PID=$!
    cd ..
    
    print_success "Development servers started!"
    print_status "Backend: http://localhost:8080"
    print_status "Frontend: http://localhost:3000"
    print_status "H2 Console: http://localhost:8080/h2-console"
    
    # Wait for user to stop servers
    echo ""
    print_warning "Press Ctrl+C to stop the development servers"
    wait
}

# Main setup function
main() {
    echo "🎫 Event Registration System Setup"
    echo "=================================="
    echo ""
    
    check_requirements
    echo ""
    
    setup_backend
    echo ""
    
    setup_frontend
    echo ""
    
    create_env_files
    echo ""
    
    read -p "Do you want to run tests? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        run_tests
        echo ""
    fi
    
    read -p "Do you want to start development servers? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        start_dev_servers
    fi
    
    echo ""
    print_success "Setup completed successfully!"
    echo ""
    echo "Next steps:"
    echo "1. Configure your AWS credentials if deploying to AWS"
    echo "2. Update environment variables in .env files"
    echo "3. Run 'cd backend && ./mvnw spring-boot:run' to start backend"
    echo "4. Run 'cd frontend && npm start' to start frontend"
    echo "5. Visit http://localhost:3000 to access the application"
    echo ""
    echo "For deployment instructions, see README.md"
}

# Run main function
main "$@" 