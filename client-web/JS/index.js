const API_URI = "http://localhost:8080";

const getDomainDetail = async (domainName) => {
  const response = await fetch(API_URI + "/domains/" + domainName);
  const data = await response.json();
  return data;
};

const getUserDomains = async (uid) => {
  const response = await fetch(API_URI + "/domains/user/" + uid);
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
  uid,
  domainName,
  registerTime,
  cardNumber,
  expDate,
  cvv,
  cardOwnerName,
  cardOwnerSurname
) => {
  const response = await fetch(API_URI + "/buy-domain/", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      uid,
      domainName,
      cardNumber,
      expDate,
      cvv,
      cardOwnerName,
      cardOwnerSurname,
    }),
  });
  const data = await response.json();
  return data;
};

const userSignUp = async (name, surname, email) => {
  const response = await fetch(API_URI + "/user/signup", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      email,
      name,
      surname,
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

const createRegisterDomainCard = (domain) => {
  const domainCard = document.createElement("div");
  domainCard.id = "domainCard";
  const domainCardText = document.createElement("p");
  domainCardText.id = "domainCardText";
  domainCardText.innerText = `Dominio: ${domain.id} - Data di registrazione: ${domain.registrationDate} - Data di scadenza: ${domain.expiration} - Stato: ${domain.status}`;
};

window.addEventListener("load", () => {
  const navDomain = document.getElementById("navDomain");
  const navUserDomain = document.getElementById("navUserDomains");
  const navOrder = document.getElementById("navOrder");
  const navSignUp = document.getElementById("navSignUp");
  const logout = document.getElementById("logout");
  const domain = document.getElementById("domain");
  const userDomains = document.getElementById("userDomains");
  const orders = document.getElementById("orders");
  const signUp = document.getElementById("signUp");
  const search = document.getElementById("searchDomainButton");
  const signUpButton = document.getElementById("signUpButton");
  const registerDomainButton = document.getElementById("registerDomainButton");
  const renewDomainButton = document.getElementById("renewDomainButton");

  if (localStorage.getItem("userUid")) {
    domain.style.display = "block";
    userDomains.style.display = "none";
    orders.style.display = "none";
    signUp.style.display = "none";
    navSignUp.style.display = "none";
    document.getElementById("navDomain").setAttribute("selected", "");
  } else {
    domain.style.display = "none";
    userDomains.style.display = "none";
    orders.style.display = "none";
    signUp.style.display = "block";
    navDomain.style.display = "none";
    navUserDomain.style.display = "none";
    navOrder.style.display = "none";
    logout.style.display = "none";
    document.getElementById("navSignUp").setAttribute("selected", "");
  }

  logout.addEventListener("click", () => {
    localStorage.removeItem("userUid");
    window.location.reload();
  });

  navDomain.addEventListener("click", () => {
    document.querySelectorAll(".navItem[selected]").forEach((item) => {
      item.removeAttribute("selected");
    });

    document.getElementById("navDomain").setAttribute("selected", "");

    domain.style.display = "block";
    userDomains.style.display = "none";
    orders.style.display = "none";
    signUp.style.display = "none";
  });

  navUserDomain.addEventListener("click", () => {
    document.querySelectorAll(".navItem[selected]").forEach((item) => {
      item.removeAttribute("selected");
    });

    document.getElementById("navUserDomains").setAttribute("selected", "");

    domain.style.display = "none";
    userDomains.style.display = "block";
    orders.style.display = "none";
    signUp.style.display = "none";

    const userDomainsList = document.getElementById("userDomainsList");

    getUserDomains(localStorage.getItem("userUid")).then((domains) => {
      domains.forEach((domain) => {
        const domainCard = createRegisterDomainCard(domain);
        userDomainsList.appendChild(domainCard);
      });
    });
  });

  navOrder.addEventListener("click", () => {
    document.querySelectorAll(".navItem[selected]").forEach((item) => {
      item.removeAttribute("selected");
    });

    document.getElementById("navOrder").setAttribute("selected", "");

    domain.style.display = "none";
    userDomains.style.display = "none";
    orders.style.display = "block";
    signUp.style.display = "none";

    getOrders(localStorage.getItem("userUid")).then((orders) => {
      const orderList = document.getElementById("orderList");
      orders.forEach((order) => {
        const orderCard = document.createElement("div");
        orderCard.id = "orderCard";
        const orderCardText = document.createElement("p");
        orderCardText.id = "orderCardText";
        orderCardText.innerText = `Dominio: ${order.domainName} - Data: ${order.date} - Tipo: ${order.type} - Prezzo: ${order.price}`;
        orderCard.appendChild(orderCardText);
        orderList.appendChild(orderCard);
      });
    });
  });

  navSignUp.addEventListener("click", () => {
    document.querySelectorAll(".navItem[selected]").forEach((item) => {
      item.removeAttribute("selected");
    });

    document.getElementById("navSignUp").setAttribute("selected", "");

    domain.style.display = "none";
    userDomains.style.display = "none";
    orders.style.display = "none";
    signUp.style.display = "block";
  });

  signUpButton.addEventListener("click", () => {
    const name = document.getElementById("name").value;
    const surname = document.getElementById("surname").value;
    const email = document.getElementById("email").value;
    const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;

    if (name === "" || surname === "" || email === "") {
      alert("Compila tutti i campi");
      return;
    }

    if (!emailRegex.test(email)) {
      alert("Inserisci una email valida");
      return;
    }

    userSignUp(name, surname, email)
      .then((data) => {
        localStorage.setItem("userUid", data.uid);
        window.location.reload();
      })
      .catch(() => {
        alert("Errore durante la registrazione");
      });
  });

  search.addEventListener("click", () => {
    const domainName = document.getElementById("domainName").value;
    const domainInfo = document.getElementById("domainInfo");
    const registerForm = document.getElementById("domainRegister");
    if (domainName.replace(/\s/g, "") === "") {
      alert("Inserisci un nome valido");
      return;
    }

    getDomainDetail(domainName)
      .then((domain) => {
        if (domain.ownership) {
          domainInfo.innerText = `Il dominio cercato appartiene gia ad un altro utente: Proprietario: ${domain.ownership.name} ${domain.ownership.surname} - Email: ${domain.ownership.email} - Data di scadenza: ${domain.expiration}`;
        } else {
          domainInfo.innerText = `Il dominio cercato è disponibile: Prezzo: ${domain.price}`;
          registerForm.style.display = "block";
        }
      })
      .catch(() => {
        domainInfo.innerText = "Dominio non trovato";
      });
  });

  registerDomainButton.addEventListener("click", () => {
    const expirationDate = document.getElementById("expirationDate").value;
    const domainName = document.getElementById("domainName").value;
    const cardNumber = document.getElementById("cardNumber").value;
    const expDate = document.getElementById("expDate").value;
    const cvv = document.getElementById("cvv").value;
    const cardFirstName = document.getElementById("cardFirstName").value;
    const cardLastName = document.getElementById("cardLastName").value;

    if (
      expirationDate === "" ||
      domainName === "" ||
      cardNumber === "" ||
      expDate === "" ||
      cvv === "" ||
      cardFirstName === "" ||
      cardLastName === ""
    ) {
      alert("Compila tutti i campi");
      return;
    }

    buyDomain(
      localStorage.getItem("userUid"),
      domainName,
      cardNumber,
      expDate,
      cvv,
      cardFirstName,
      cardLastName
    )
      .then(() => {
        alert("Dominio acquistato con successo");
      })
      .catch(() => {
        alert("Errore durante l'acquisto");
      });
  });
});
