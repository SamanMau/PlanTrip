
      function openSpotifyModal() {
      document.getElementById("spotifyModal").style.display = "block";
    }

    function closeSpotifyModal() {
      document.getElementById("spotifyModal").style.display = "none";
    }

    function authenticateSpotify() {
      const spotifyUrl = "https://accounts.spotify.com/authorize?client_id=72a9d8f2ee974b11b040c55d4319f934&response_type=code&redirect_uri=http%3A%2F%2F127.0.0.1%3A8080%2Fapi%2Fcallback&scope=playlist-modify-public%20user-read-private&show_dialog=true";
      window.open(spotifyUrl, "_blank");
    }

    function getRecomendations(){
        const url = `http://127.0.0.1:8080/api/musicRecommendations`;
        
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

    }
