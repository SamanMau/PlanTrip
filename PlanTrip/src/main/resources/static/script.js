document.addEventListener("DOMContentLoaded", function () {
  const form = document.getElementById("tripForm");

  form.addEventListener("submit", function (e) {
    e.preventDefault();

    // Hämta värden från formuläret
    const from = document.getElementById("fromCity").value;
    const to = document.getElementById("toCity").value;
    const date = document.getElementById("departureDate").value;
    const adults = document.getElementById("adults").value;
    const children = document.getElementById("children").value;
    const infants = document.getElementById("infants").value;
    const travelClass = document.getElementById("travelClass").value;
    const maxPrice = document.getElementById("maxPrice").value;
    const currency = document.getElementById("currency").value;

    // Bygg URL med alla parametrar
    const url = `http://localhost:8080/api/trip?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&date=${encodeURIComponent(date)}&adults=${encodeURIComponent(adults)}&children=${encodeURIComponent(children)}&infants=${encodeURIComponent(infants)}&travelClass=${encodeURIComponent(travelClass)}&maxPrice=${encodeURIComponent(maxPrice)}&currency=${encodeURIComponent(currency)}`;

    fetch(url)
      .then(response => {
        if (!response.ok) throw new Error("Något gick fel vid hämtning av data");
        return response.json(); // Backend skickar en ArrayList<String>
      })
      .then(data => {
        const resultsSection = document.getElementById("results");
        resultsSection.innerHTML = "<h2>✈️ Flight Results</h2>";

        const maxFlights = 4;
        const displayedFlights = data.slice(0, maxFlights);

        displayedFlights.forEach(flightInfo => {
          const flightCard = document.createElement("div");
          flightCard.className = "flight-card";

          const lines = flightInfo.split("\n");
          lines.forEach(line => {
            const p = document.createElement("p");
            p.textContent = line.trim();
            flightCard.appendChild(p);
          });

          resultsSection.appendChild(flightCard);
        });
      })
      .catch(error => {
        console.error("Fel vid API-anrop:", error);
        const resultsSection = document.getElementById("results");
        resultsSection.innerHTML = "<p>Något gick fel när data skulle hämtas. Försök igen senare.</p>";
      });
  });
});
