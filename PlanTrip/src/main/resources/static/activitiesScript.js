document.addEventListener("DOMContentLoaded", getActivities);

function getActivities() {
    fetch("http://127.0.0.1:8080/api/fetch-activities")
        .then(response => {
            if (!response.ok) throw new Error("Failed to fetch activities");
            return response.json();
        })
        .then(data => {
            // Här förväntas "data" vara en ArrayList<HashMap<String,String>>
            console.log("Activities received:", data);
            renderActivities(data);
        })
        .catch(err => console.error("Error fetching activities:", err));
}

function renderActivities(activities) {
    const grid = document.querySelector(".activity-grid");
    grid.innerHTML = ""; // Rensa innan vi renderar nytt

    activities.forEach(activity => {
        const card = document.createElement("div");
        card.classList.add("activity-card");

        card.innerHTML = `
            <div class="activity-image" style="background-image: url('${activity.picture}'); background-size: cover;">
            </div>

            <div class="activity-content">
                <h3 class="activity-title">${activity.title}</h3>
                <p class="activity-desc">${activity.description}</p>
            </div>
        `;

        grid.appendChild(card);
    });
}
