// Criar usuário para a aplicação
db.createUser({
    user: "wallet_user",
    pwd: "wallet_pass",
    roles: [
        {
            role: "readWrite",
            db: "wallet_db"
        }
    ]
});

// Criar índices otimizados
db.wallets.createIndex({"userId": 1}, {unique: true});
db.wallets.createIndex({"createdAt": 1});
db.wallets.createIndex({"updatedAt": 1});

db.wallet_transactions.createIndex({"walletId": 1});
db.wallet_transactions.createIndex({"timestamp": 1});
db.wallet_transactions.createIndex({"correlationId": 1});
db.wallet_transactions.createIndex({"walletId": 1, "timestamp": 1});

print("MongoDB initialization completed successfully!");