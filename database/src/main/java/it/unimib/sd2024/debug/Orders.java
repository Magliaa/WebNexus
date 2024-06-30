package it.unimib.sd2024.debug;

public class Orders {
    static String orders = """
        {
            "1": {
                "cardCvv": "123",
                "cardExpireDate": "08-28",
                "cardId": "5338161236248805",
                "cardName": "Omar",
                "cardSurname": "The SD Enjoyer",
                "domain": "domain0.com",
                "price": 100000,
                "type": "register",
                "userId": "1"
            },
            "2": {
                "cardCvv": "345",
                "cardExpireDate": "08-26",
                "cardId": "5338161233348805",
                "cardName": "Beppino",
                "cardSurname": "The SD Enjoyer",
                "domain": "domain1.com",
                "price": 10000,
                "type": "register",
                "userId": "2"
            },
            "3": {
                "cardCvv": "444",
                "cardExpireDate": "04-30",
                "cardId": "5338432236248805",
                "cardName": "Giovanni",
                "cardSurname": "The SD Enjoyer",
                "domain": "domain10.com",
                "price": 100000,
                "type": "register",
                "userId": "3"
            }
        }
    """;
}
