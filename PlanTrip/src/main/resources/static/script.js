let tripChosen = false;
document.addEventListener("DOMContentLoaded", function () {
form = document.getElementById("tripForm");

  if(form){
      form.addEventListener("submit", function (e) {
    e.preventDefault();

    const from = document.getElementById("fromCity").value;
    const to = document.getElementById("toCity").value;
    const date = document.getElementById("departureDate").value;
    const adults = document.getElementById("adults").value;
    const children = document.getElementById("children").value;
    const infants = document.getElementById("infants").value;
    tripChosen = true;

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
  }

});

      function openSpotifyModal() {
      if(!tripChosen) {
        alert("Please plan your trip first!");
          return;
        }
      document.getElementById("spotifyModal").style.display = "block";
    }

    function closeSpotifyModal() {
      document.getElementById("spotifyModal").style.display = "none";
    }

    function authenticateSpotify() {
      const spotifyUrl = "https://accounts.spotify.com/authorize?client_id=72a9d8f2ee974b11b040c55d4319f934&response_type=code&redirect_uri=http%3A%2F%2F127.0.0.1%3A8080%2Fapi%2Fcallback&scope=playlist-modify-public%20user-read-private&show_dialog=true";
      window.open(spotifyUrl, "_blank");
    }
