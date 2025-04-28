document.addEventListener("DOMContentLoaded", function () {
  const form = document.getElementById("tripForm");

  form.addEventListener("submit", function (e) {
    e.preventDefault();

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
    const url = `/api/trip?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&date=${encodeURIComponent(date)}&adults=${encodeURIComponent(adults)}&children=${encodeURIComponent(children)}&infants=${encodeURIComponent(infants)}&travelClass=${encodeURIComponent(travelClass)}&maxPrice=${encodeURIComponent(maxPrice)}&currency=${encodeURIComponent(currency)}`;

    fetch(url)
      .then(response => {
        if (!response.ok) throw new Error("Något gick fel vid hämtning av data");
        return response.json();
      })
      .then(data => {
        console.log("Svar från servern:", data);
        // TODO: uppdatera HTML med API-svaret
      })
      .catch(error => {
        console.error("Fel vid API-anrop:", error);
      });
  });
});