db = db.getSiblingDB('orderflow');

// Create collections with schema validation
db.createCollection('orders', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['customerId', 'customerName', 'items', 'status', 'totalAmount'],
      properties: {
        customerId: { bsonType: 'string' },
        customerName: { bsonType: 'string' },
        status: { enum: ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'] }
      }
    }
  }
});

db.createCollection('outbox_events');

// Indexes for orders
db.orders.createIndex({ customerId: 1, createdAt: -1 });
db.orders.createIndex({ status: 1, createdAt: -1 });
db.orders.createIndex({ createdAt: -1 });

// Indexes for outbox (polling pattern)
db.outbox_events.createIndex({ published: 1, createdAt: 1 });

print('MongoDB initialized for OrderFlow');