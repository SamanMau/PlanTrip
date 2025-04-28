document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("tripForm");
  
    form.addEventListener("submit", function (e) {
      e.preventDefault(); // Förhindrar att sidan laddas om
  
      const from = document.getElementById("fromCity").value;
      const to = document.getElementById("toCity").value;
      const date = document.getElementById("departureDate").value;
      const budget = document.getElementById("budget").value;
  
      // Skapa URL med query-parametrar
      const url = `/api/trip?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&date=${encodeURIComponent(date)}&budget=${encodeURIComponent(budget)}`;
  
      fetch(url)
        .then(response => {
          if (!response.ok) throw new Error("Något gick fel");
          return response.json();
        })
        .then(data => {
          console.log("Svar från servern:", data);
          // Här kan du uppdatera HTML med svar från backend
        })
        .catch(error => {
          console.error("Fel vid anrop:", error);
        });
    });
  });