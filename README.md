QuotePilot
AI-Powered RFx & Quote Analysis Platform
QuotePilot helps procurement teams evaluate vendor quotations faster and make more transparent, data-driven award decisions. It brings RFx management, quote extraction, side-by-side comparison, AI-assisted analysis, and award recommendations into one workflow.
Project status: Prototype / take-home assignment demo. Some integrations and workflows are simulated; see Implementation Notes.

Overview
Procurement teams often receive vendor quotes in different formats, with inconsistent units, incomplete item coverage, and varying commercial terms. Comparing them manually can be time-consuming and error-prone.

QuotePilot is designed to help users:
- Create and manage RFx requests and line items.
- Upload vendor quotations and extract quote information.
- Compare vendor pricing across items.
- Identify missing quotes, unit mismatches, and other exceptions.
- Ask natural-language questions about quote data.
- Generate explainable award recommendations and review readiness.
Features
- RFx workspace: View and manage RFx requests and items.
- Quote ingestion: Upload vendor quote files and extract relevant information.
- Quote comparison: Compare vendor prices and item coverage in one view.
- Exception awareness: Surface incomplete quote coverage and unit inconsistencies.
- AI analyst chat: Ask questions about vendors, items, pricing, and quote comparisons.
- Award recommendation: Review a recommendation with supporting rationale and readiness information.
- AI-assisted RFx drafting: Generate a draft RFx using an AI prompt.
Demo Scenario
The prototype is designed around a sourcing scenario with:
- 5 vendors
- 30 line items
- Mixed quote formats
- A partially quoted vendor (27 of 30 items)
- USD pricing
- Unit mismatches requiring review
The scenario is intended to demonstrate how QuotePilot can make quote analysis and sourcing decisions easier to inspect.
Architecture
React + TypeScript + Vite
          |
          | REST API
          v
Spring Boot (Java 21)
          |
          +------ PostgreSQL
          |
          +------ Groq API (AI features)
Tech Stack
Layer	Technology
Frontend	React, TypeScript, Vite
Backend	Java 21, Spring Boot
Database	PostgreSQL
AI	Groq API
Frontend hosting (planned)	Vercel
Backend & database hosting (planned)	Render


Getting Started
Prerequisites
Install the following before running the application:
- Node.js and npm
- Java 21
- Maven (or use the Maven wrapper if included)
- PostgreSQL
- A Groq API key for AI-powered features
1. Clone the repository
git clone YOUR_GITHUB_REPOSITORY_URL
cd QuotePilot
2. Run the frontend
From the frontend project directory:
npm install
npm run dev
Vite will print the local development URL in the terminal.
3. Run the backend
From the Spring Boot backend directory:
./mvnw spring-boot:run
On Windows:
.\mvnw.cmd spring-boot:run
If the project does not include the Maven wrapper, use:
mvn spring-boot:run
4. Configure environment variables
Configure database credentials, the Groq API key, and any frontend API base URL using the variable names expected by the application.

Do not commit API keys, passwords, or production credentials. Keep local secrets in an ignored .env file or your local application configuration, and configure production secrets in the hosting provider's environment settings.
The exact environment variable names depend on the application's configuration. Check the backend configuration files before setting them.

Typical Workflow
1. Open an RFx and review its line items.
2. Upload vendor quotation files.
3. Inspect extracted quote details and exceptions.
4. Compare pricing and coverage across vendors.
5. Ask the AI analyst questions about the quotes.
6. Review the award recommendation, rationale, and readiness.
7. Use the AI RFx drafting workflow when creating a new sourcing request.
Example Analyst Questions
Try questions such as:
- Which vendor has the lowest total quoted price?
- Which items are missing from a vendor's quotation?
- Where are unit mismatches present?
- Compare the pricing of two vendors for the same items.
- What factors support the award recommendation?
- What uncertainties should be reviewed before approving an award?
Answers depend on the data available in the current RFx and the implementation of the relevant AI workflow.
Implementation Notes
This repository is a prototype built for demonstration and evaluation.
- Vendor invitations are simulated in the demo.
- Demo vendor and quote data may be fabricated.
- AI extraction, analysis, and drafting depend on the configured backend and Groq integration.
- Verify extracted values and AI-generated recommendations before making real procurement decisions.
- A demo workflow should not be treated as a production procurement or approval system.
Roadmap
Potential next steps:
- Integrate real vendor invitation and response workflows.
- Add robust validation and human review for extracted quote data.
- Improve audit trails and approval controls.
- Add authentication, authorization, and role-based access.
- Add production monitoring, tests, and deployment automation.
Security
- Never commit .env files or API keys.
- Use environment variables or a secrets manager for credentials.
- Do not upload confidential procurement documents unless you have permission.
- Review AI outputs and extracted data before acting on them.
Author
Melvia Vinilda Dsouza
