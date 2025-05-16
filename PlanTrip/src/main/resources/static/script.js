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

    if (adults < 1) {
      alert("At least one adult is required");
      return;
    }

    if (infants > adults) {
      alert("Number of infants can't exceed number of adults");
      return;
    }

    const travelClass = document.getElementById("travelClass").value;
    const maxPrice = document.getElementById("maxPrice").value;
    const currency = document.getElementById("currency").value;

    const url = `http://localhost:8080/api/trip?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&date=${encodeURIComponent(date)}&adults=${encodeURIComponent(adults)}&children=${encodeURIComponent(children)}&infants=${encodeURIComponent(infants)}&travelClass=${encodeURIComponent(travelClass)}&maxPrice=${encodeURIComponent(maxPrice)}&currency=${encodeURIComponent(currency)}`;

    fetch(url)
      .then(response => {
        if (!response.ok) throw new Error("Något gick fel vid hämtning av data");
        return response.json();
      })
      .then(data => {
        const resultsSection = document.getElementById("results");
        resultsSection.innerHTML = "<h2>✈️ Flight Results</h2>";

        const maxFlights = 12;
        const displayedFlights = data.slice(0, maxFlights);

        let shortestDuration = Infinity;

        displayedFlights.forEach(flightInfo => {
          const flightCard = document.createElement("div");
          flightCard.className = "flight-card";

          const lines = flightInfo.split("\n");

          lines.forEach((line) => {
            const p = document.createElement("p");
            p.textContent = line.trim();
            p.style.marginBottom = "8px";
            flightCard.appendChild(p);

            if (line.startsWith("⏳ Flight duration:")) {
              const durationStr = line.replace("⏳ Flight duration:", "").trim();
              const match = durationStr.match(/(?:(\d+)h)?\s*(?:(\d+)m)?/);

              if (match) {
                const hours = parseInt(match[1]) || 0;
                const minutes = parseInt(match[2]) || 0;
                const totalMinutes = hours * 60 + minutes;

                if (totalMinutes < shortestDuration) {
                  shortestDuration = totalMinutes;
                }
              }
            }
          });

          resultsSection.appendChild(flightCard);
        });

        if (shortestDuration !== Infinity) {
          localStorage.setItem("flightDuration", shortestDuration.toString());
        }
      })
      .catch(error => {
        console.error("Fel vid API-anrop:", error);
        const resultsSection = document.getElementById("results");
        resultsSection.innerHTML = "<p>No flights were found</p>";
      });
  });
});

document.addEventListener("DOMContentLoaded", function () {
  const duration = localStorage.getItem("flightDuration");

  if (duration) {
    const url = `http://localhost:8080/api/music-recommendations?duration=${encodeURIComponent(duration)}`;

    fetch(url)
      .then(response => {
        if (!response.ok) throw new Error("Failed to fetch music data");
        return response.json();
      })
      .then(data => {
        const resultDiv = document.getElementById("musicResult");
        for (const [key, value] of Object.entries(data)) {
          const p = document.createElement("p");

          if (value.startsWith("http")) {
            const a = document.createElement("a");
            a.href = value;
            a.target = "_blank";
            a.textContent = `${key}: ${value}`;
            p.appendChild(a);
          } else {
            p.textContent = `${key}: ${value}`;
          }

          resultDiv.appendChild(p);
        }
      })
      .catch(err => {
        console.error("Error:", err);
      });
  } else {
    alert("No flight duration found. Please go back and search for a trip first.");
  }
});
