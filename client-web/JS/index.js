const API_URI = "http://localhost:8080";

const getDomains = async () => {
  const response = await fetch(API_URI + "/domains/");
  const data = await response.json();
  return data;
};

const getUserDomains = async (uid) => {
  const response = await fetch(API_URI + "/domains/" + uid);
  const data = await response.json();
  return data;
};

const getUserDetail = async (uid) => {
  const response = await fetch(API_URI + "/users/" + uid);
  const data = await response.json();
  return data;
};

const getOrders = async (uid) => {
  const response = await fetch(API_URI + "/orders/" + uid);
  const data = await response.json();
  return data;
};

const buyDomain = async (
  username,
  firstName,
  lastName,
  email,
  cardNumber,
  expDate,
  cvv,
  cardFirstName,
  cardLastName
) => {
  const user = {
    username,
    firstName,
    lastName,
    email,
  };
  const creditCard = {
    cardNumber,
    expDate,
    cvv,
    cardFirstName,
    cardLastName,
  };
  const response = await fetch(API_URI + "/buy-domain/", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      user,
      creditCard,
    }),
  });
  const data = await response.json();
  return data;
};

const userSignUp = async (username, firstName, lastName, email) => {
  const response = await fetch(API_URI + "/user/", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      username,
      firstName,
      lastName,
      email,
    }),
  });
  const data = await response.json();
  return data;
};

const renewDomain = async (id, renewPeriod) => {
  const response = await fetch(API_URI + "/renew-domain/", {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      id,
      renewPeriod,
    }),
  });
  const data = await response.json();
  return data;
};

window.addEventListener("load", () => {
  getDomains().then((domains) => {
    const domainList = document.getElementById("domainList");
    domains.forEach((domain) => {
      const domainCard = document.createElement("div");
      domainCard.classList.add("card");

      const domainCardBody = document.createElement("div");
      domainCardBody.classList.add("card-body");

      const domainCardText = document.createElement("p");
      domainCardText.classList.add("card-text");
      domainCardText.textContent = `Dominio: ${domain.id} - Prezzo: ${domain.price} - Data di scadenza: ${domain.expirationDate}`;

      domainCardBody.appendChild(domainCardText);
      domainCard.appendChild(domainCardBody);
      domainList.appendChild(domainCard);
    });
  });
});
