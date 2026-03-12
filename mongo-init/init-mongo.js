// MongoDB init script – runs once when the data volume is empty.
// To reset: docker-compose down -v && docker-compose up

db = db.getSiblingDB('btg_funds');

// ---------------------------------------------------------------------------
// Funds
// ---------------------------------------------------------------------------
db.funds.insertMany([
  {
    _id: "fund-001",
    code: "FPV_BTG_PACTUAL_RECAUDADORA",
    name: "FPV BTG Pactual Recaudadora",
    minAmount: NumberDecimal("75000"),
    category: "FPV"
  },
  {
    _id: "fund-002",
    code: "FPV_BTG_PACTUAL_ECOPETROL",
    name: "FPV BTG Pactual Ecopetrol",
    minAmount: NumberDecimal("125000"),
    category: "FPV"
  },
  {
    _id: "fund-003",
    code: "DEUDAPRIVADA",
    name: "Deuda Privada",
    minAmount: NumberDecimal("50000"),
    category: "FIC"
  },
  {
    _id: "fund-004",
    code: "FDO-ACCIONES",
    name: "Fondo Acciones",
    minAmount: NumberDecimal("250000"),
    category: "FIC"
  },
  {
    _id: "fund-005",
    code: "FPV_BTG_PACTUAL_DINAMICA",
    name: "FPV BTG Pactual Dinámica",
    minAmount: NumberDecimal("100000"),
    category: "FPV"
  }
]);

db.funds.createIndex({ code: 1 }, { unique: true });

// ---------------------------------------------------------------------------
// Users  (password = "password123" hashed with BCrypt cost 10)
// ---------------------------------------------------------------------------
var bcryptHash = "$2a$10$EqKcp1WGKWJlpvnGhBRvhODwGbQ1cVEryVtKIFoOkTUwJBdGbVHMa";

db.users.insertMany([
  {
    _id: "user-001",
    email: "carlos.martinez@example.com",
    passwordHash: bcryptHash,
    balance: NumberDecimal("500000"),
    role: "USER",
    notificationPreference: {
      channel: "EMAIL",
      email: "carlos.martinez@example.com",
      phoneNumber: "+573001234567"
    }
  },
  {
    _id: "user-002",
    email: "maria.lopez@example.com",
    passwordHash: bcryptHash,
    balance: NumberDecimal("500000"),
    role: "USER",
    notificationPreference: {
      channel: "SMS",
      email: "maria.lopez@example.com",
      phoneNumber: "+573009876543"
    }
  },
  {
    _id: "user-003",
    email: "andres.garcia@example.com",
    passwordHash: bcryptHash,
    balance: NumberDecimal("500000"),
    role: "USER",
    notificationPreference: {
      channel: "EMAIL",
      email: "andres.garcia@example.com",
      phoneNumber: "+573005551234"
    }
  },
  {
    _id: "user-004",
    email: "laura.rodriguez@example.com",
    passwordHash: bcryptHash,
    balance: NumberDecimal("500000"),
    role: "USER",
    notificationPreference: {
      channel: "EMAIL",
      email: "laura.rodriguez@example.com",
      phoneNumber: "+573007778899"
    }
  },
  {
    _id: "user-005",
    email: "admin@btgpactual.com",
    passwordHash: bcryptHash,
    balance: NumberDecimal("500000"),
    role: "ADMIN",
    notificationPreference: {
      channel: "EMAIL",
      email: "admin@btgpactual.com",
      phoneNumber: "+573000000000"
    }
  }
]);

db.users.createIndex({ email: 1 }, { unique: true });

print("✔ Seed data loaded: 5 funds + 5 users");
