const API_URI = "http://localhost:8080";

// get a "domains/{{domainName}}, ritorna le informazioni del dominio
const getDomainDetail = async (domainName) => {
  const response = await fetch(API_URI + "/domains/" + domainName);
  const data = await response.json();
  return data;
};

// get a "domains/user/{userId}, ritorna i domini registrati dall'utente
const getUserDomains = async (uid) => {
  const response = await fetch(API_URI + "/domains/user/" + uid);
  const data = await response.json();
  return data;
};

// get a /orders/{userId}, ritorna gli ordini dell'utente (anche quelli scaduti)
const getOrders = async (uid) => {
  const response = await fetch(API_URI + "/orders/" + uid);
  const data = await response.json();
  return data;
};

// post a /domains/register/, permette di registrare un dominio
const buyDomain = async (
  uid,
  domainName,
  registerTime,
  cardNumber,
  cvv,
  cardOwnerName,
  cardOwnerSurname
) => {
  const response = await fetch(API_URI + "/domains/register/", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      uid,
      domainName,
      registerTime,
      cardNumber,
      cvv,
      cardOwnerName,
      cardOwnerSurname,
    }),
  });
  const data = await response.json();
  return data;
};

// post a /user/signup, permette di registrare un utente
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

// post a /renew-domain permette di rinnovare un dominio
const renewDomain = async (userId, domainName, renewTime) => {
  const response = await fetch(API_URI + "/domains/renew", {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      userId,
      domainName,
      renewTime,
    }),
  });
  const data = await response.json();
  return data;
};

// funzione per costruire dinamicamente una card di dominio registrato
const createRegisterDomainCard = (name, domain) => {
  const domainCard = document.createElement("div");
  domainCard.id = "domainCard";
  const domainCardText = document.createElement("p");
  domainCardText.id = "domainCardText";
  domainCardText.innerText = `Dominio: ${name} - Data di registrazione: ${domain.registerDate} - Data di scadenza: ${domain.expirationDate}`;
  domainCard.append(domainCardText)
  return domainCard
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

  // gestione della registrazione tramite localStorage, dopo la registrazione l'utente mantiene l'accesso fino al logout
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

  // gestione del logout
  logout.addEventListener("click", () => {
    localStorage.removeItem("userUid");
    window.location.reload();
  });

  // gestione della navbar per simulare una SPA (SinglePageApplication)
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

  // gestione della navbar per simulare una SPA (SinglePageApplication)
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
      userDomainsList.innerHTML = ''
      const keys = Object.keys(domains)
      keys.forEach((key) => {
        const domainCard = createRegisterDomainCard(key, domains[key]);
        userDomainsList.appendChild(domainCard);
      });
    });
  });

  // gestione della navbar per simulare una SPA (SinglePageApplication)
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
      const orderList = document.getElementById("ordersList");
      orderList.innerHTML = ''
      const keys = Object.keys(orders)
      keys.forEach((key) => {
        const order = orders[key]
        const orderCard = document.createElement("div");
        orderCard.id = "orderCard";
        const orderCardText = document.createElement("p");
        orderCardText.id = "orderCardText";
        orderCardText.innerText = `Dominio: ${order.domain} - Data di scadenza: ${order.cardExpireDate} - Tipo: ${order.type} - Prezzo: ${order.price}`;
        orderCard.appendChild(orderCardText);
        orderList.appendChild(orderCard);
      });

      if (keys.length === 0) {
        orderList.innerHTML = 'Nessun ordine effettuato!'
      }
    });
  });

  // gestione della navbar per simulare una SPA (SinglePageApplication)
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

  // gestione dell'evento di registrazione, (prende tutti i dati dal form e li invia tramite api)
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

  // gestione dell'evento di ricerca per nome dominio
  search.addEventListener("click", () => {
    const domainName = document.getElementById("domainName").value;
    const domainInfo = document.getElementById("domainInfo");
    const registerForm = document.getElementById("domainRegister");
    if (domainName.replace(/\s/g, "") === "") {
      alert("Inserisci un nome valido");
      return;
    }

    getDomainDetail(domainName)
      .then((risp) => {
        const domain = risp.domain
        const user = risp.user
        domainInfo.innerHTML = ''
        registerForm.style.display = 'none'
        if (domain.ownershipUserId) {
          domainInfo.innerText = `\n Il dominio cercato appartiene fino al ${domain.expirationDate} all'utente: ${user.name} ${user.surname} - email: ${user.email}`;
        } else {
          registerForm.style.display = "block";
        }
      }).catch(() => {
        domainInfo.innerHTML = ''
        registerForm.style.display = "block";
    })
  });

  // gestione del pulsante di registra dominio, (prende tutti i dati dal form e li invia tramite api)
  registerDomainButton.addEventListener("click", () => {
    const registerTime = document.getElementById("registerTime").value;
    const domainName = document.getElementById("domainName").value;
    const cardNumber = document.getElementById("cardNumber").value;
    const cvv = document.getElementById("cvv").value;
    const cardFirstName = document.getElementById("cardFirstName").value;
    const cardLastName = document.getElementById("cardLastName").value;

    if (
      domainName === "" ||
      cardNumber === "" ||
      cvv === "" ||
      cardFirstName === "" ||
      cardLastName === ""
    ) {
      alert("Compila tutti i campi");
      return;
    }

    if (registerTime > 10 || registerTime < 0) {
      alert("Il tempo di registrazione può essere al massimo 10 anni e al minimo 0")
      return
    }

    buyDomain(
      localStorage.getItem("userUid"),
      domainName,
      registerTime,
      cardNumber,
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

  renewDomainButton.addEventListener("click", () => {
    const domainName = document.getElementById("renewDomainName").value
    const renewTime = document.getElementById("renewDomainDate").value

    if (domainName === '' || renewTime === '' || renewTime > 10 || renewTime < 0) {
      alert("Compila i campi correttamente!")
      return
    }

    renewDomain(
        localStorage.getItem("userUid"),
        domainName,
        renewTime
    ).then(() => {
      alert("Rinnovo avvenuto correttamente")
      const userDomainsList = document.getElementById("userDomainsList");

      getUserDomains(localStorage.getItem("userUid")).then((domains) => {
        userDomainsList.innerHTML = ''
        const keys = Object.keys(domains)
        keys.forEach((key) => {
          const domainCard = createRegisterDomainCard(key, domains[key]);
          userDomainsList.appendChild(domainCard);
        });
      });
    }).catch(() => {
      alert("Errore durante il rinnovo del dominio!")
    })
  })

});
