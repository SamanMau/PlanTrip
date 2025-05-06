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
        return response.json(); // Förväntar sig en ArrayList av HashMaps som konverteras till array av objekt i JS
      })
      .then(data => {
        console.log("Svar från servern:", data);
      
        const resultsSection = document.getElementById("results");
        resultsSection.innerHTML = ""; // Töm tidigare resultat
      
        if (data.length === 0) {
          resultsSection.innerHTML = "<p>Inga flygresor hittades för dina kriterier.</p>";
          return;
        }
      
        data.forEach((flightString, index) => {
          const tripCard = document.createElement("div");
          tripCard.className = "trip-card";
      
          tripCard.innerHTML = `
            <h3>Flyg ${index + 1}</h3>
            <p>${flightString}</p>
          `;
      
          resultsSection.appendChild(tripCard);
        });
      })
      .catch(error => {
        console.error("Fel vid API-anrop:", error);
        const resultsSection = document.getElementById("results");
        resultsSection.innerHTML = "<p>Något gick fel när data skulle hämtas. Försök igen senare.</p>";
      });
  });
});
