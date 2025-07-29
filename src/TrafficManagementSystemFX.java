import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import java.util.*;

public class TrafficManagementSystemFX extends Application {

    // Class to represent a Road (copied from original)
    static class Road {
        String destination;
        int distance;
        int trafficLevel; // 1-10, where 10 is very congested

        public Road(String destination, int distance, int trafficLevel) {
            this.destination = destination;
            this.distance = distance;
            this.trafficLevel = trafficLevel;
        }
    }

    // Helper class to store route information (copied from original)
    static class RouteOption {
        List<String> path;
        int time;

        public RouteOption(List<String> path, int time) {
            this.path = path;
            this.time = time;
        }
    }

    // Main system class with core functionality (largely from original)
    static class TrafficManagementSystem {
        // Map to represent the city road network
        private Map<String, List<Road>> cityMap;
        // Store location coordinates for visualization
        private Map<String, Pair<Double, Double>> locationCoordinates;

        public TrafficManagementSystem() {
            cityMap = new HashMap<>();
            locationCoordinates = new HashMap<>();
        }

        // Add a new intersection/location to the map
        public void addIntersection(String location) {
            if (!cityMap.containsKey(location)) {
                cityMap.put(location, new ArrayList<>());
            }
        }

        // Set coordinates for a location (for visualization)
        public void setLocationCoordinates(String location, double x, double y) {
            locationCoordinates.put(location, new Pair<>(x, y));
        }

        // Get coordinates for a location
        public Pair<Double, Double> getLocationCoordinates(String location) {
            return locationCoordinates.get(location);
        }

        // Get all locations
        public Set<String> getAllLocations() {
            return cityMap.keySet();
        }

        // Get all roads
        public Map<String, List<Road>> getCityMap() {
            return cityMap;
        }

        // Add a road between two intersections
        public void addRoad(String from, String to, int distance, int trafficLevel) {
            // Ensure both locations exist
            addIntersection(from);
            addIntersection(to);

            // Add the road
            cityMap.get(from).add(new Road(to, distance, trafficLevel));
        }

        // Update traffic conditions on a road
        public void updateTraffic(String from, String to, int newTrafficLevel) {
            if (cityMap.containsKey(from)) {
                for (Road road : cityMap.get(from)) {
                    if (road.destination.equals(to)) {
                        road.trafficLevel = newTrafficLevel;
                        break;
                    }
                }
            }
        }

        // Get traffic level for a road
        public int getTrafficLevel(String from, String to) {
            if (cityMap.containsKey(from)) {
                for (Road road : cityMap.get(from)) {
                    if (road.destination.equals(to)) {
                        return road.trafficLevel;
                    }
                }
            }
            return -1; // Road not found
        }

        // Calculate total time for a route based on distance and traffic
        public int getRouteTime(List<String> route) {
            int totalTime = 0;

            for (int i = 0; i < route.size() - 1; i++) {
                String from = route.get(i);
                String to = route.get(i + 1);

                // Find the road between these locations
                for (Road road : cityMap.get(from)) {
                    if (road.destination.equals(to)) {
                        // Calculate time: base + traffic impact
                        int time = road.distance * (1 + (road.trafficLevel / 5));
                        totalTime += time;
                        break;
                    }
                }
            }

            return totalTime;
        }

        // Find the fastest route between two locations using Dijkstra's algorithm
        public List<String> findFastestRoute(String start, String end) {
            // If start and end are the same, return just that location
            if (start.equals(end)) {
                return Collections.singletonList(start);
            }

            // Priority queue for Dijkstra's algorithm
            PriorityQueue<Map.Entry<String, Integer>> pq = new PriorityQueue<>(
                    Comparator.comparingInt(Map.Entry::getValue)
            );
            pq.add(new AbstractMap.SimpleEntry<>(start, 0));

            // Track visited nodes
            Set<String> visited = new HashSet<>();

            // Track distances
            Map<String, Integer> distances = new HashMap<>();
            for (String location : cityMap.keySet()) {
                distances.put(location, Integer.MAX_VALUE);
            }
            distances.put(start, 0);

            // Track previous nodes for path reconstruction
            Map<String, String> previous = new HashMap<>();

            while (!pq.isEmpty()) {
                Map.Entry<String, Integer> current = pq.poll();
                String currentLocation = current.getKey();

                if (currentLocation.equals(end)) {
                    break; // Found destination
                }

                if (visited.contains(currentLocation)) {
                    continue;
                }

                visited.add(currentLocation);

                // Check all neighbors
                if (cityMap.containsKey(currentLocation)) {
                    for (Road road : cityMap.get(currentLocation)) {
                        String neighbor = road.destination;

                        if (visited.contains(neighbor)) {
                            continue;
                        }

                        // Calculate time based on distance and traffic
                        int time = road.distance * (1 + (road.trafficLevel / 5));
                        int newDistance = distances.get(currentLocation) + time;

                        if (newDistance < distances.get(neighbor)) {
                            distances.put(neighbor, newDistance);
                            previous.put(neighbor, currentLocation);
                            pq.add(new AbstractMap.SimpleEntry<>(neighbor, newDistance));
                        }
                    }
                }
            }

            // Reconstruct path if end was reached
            if (!previous.containsKey(end)) {
                return new ArrayList<>(); // No path found
            }

            List<String> path = new ArrayList<>();
            String current = end;

            while (current != null) {
                path.add(0, current);
                current = previous.get(current);
            }

            return path;
        }
    }

    // JavaFX specific members
    private TrafficManagementSystem cityNav;
    private Canvas mapCanvas;
    private final int CANVAS_WIDTH = 800;
    private final int CANVAS_HEIGHT = 600;
    private ComboBox<String> startLocationComboBox;
    private ComboBox<String> endLocationComboBox;
    private TextArea routeInfoTextArea;
    private List<String> currentDisplayedRoute = new ArrayList<>();
    private List<RouteOption> currentRouteOptions = new ArrayList<>();
    private Button nextRouteButton;
    private Button prevRouteButton;
    private Label currentRouteLabel;
    private int currentRouteIndex = 0;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Traffic Management System");

        // Create city navigation system
        initializeTrafficSystem();

        // Create layout
        BorderPane root = new BorderPane();

        // Create canvas for map visualization
        mapCanvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        Pane mapPane = new Pane(mapCanvas);
        mapPane.setStyle("-fx-background-color: #f0f0f0;");

        // Controls for route selection
        VBox controlsPane = createControlsPane();

        // Add components to root layout
        root.setCenter(mapPane);
        root.setRight(controlsPane);

        // Create scene
        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setScene(scene);

        // Draw initial map
        drawMap(null);

        primaryStage.show();
    }

    private VBox createControlsPane() {
        VBox controlsPane = new VBox(15);
        controlsPane.setPadding(new Insets(20));
        controlsPane.setPrefWidth(380);
        controlsPane.setStyle("-fx-background-color: white;");

        // Title
        Text title = new Text("Route Planner");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));

        // Location selection
        List<String> locations = new ArrayList<>(cityNav.getAllLocations());
        Collections.sort(locations);

        Label startLabel = new Label("Starting Point:");
        startLocationComboBox = new ComboBox<>(FXCollections.observableArrayList(locations));
        startLocationComboBox.setPrefWidth(340);

        Label endLabel = new Label("Destination:");
        endLocationComboBox = new ComboBox<>(FXCollections.observableArrayList(locations));
        endLocationComboBox.setPrefWidth(340);

        Button findRouteButton = new Button("Find Routes");
        findRouteButton.setPrefWidth(340);
        findRouteButton.setOnAction(e -> findRoutes());

        // Route navigation
        HBox routeNavPane = new HBox(10);
        prevRouteButton = new Button("◀ Previous");
        nextRouteButton = new Button("Next ▶");
        currentRouteLabel = new Label("Route: 0/0");

        prevRouteButton.setDisable(true);
        nextRouteButton.setDisable(true);

        prevRouteButton.setOnAction(e -> showPreviousRoute());
        nextRouteButton.setOnAction(e -> showNextRoute());

        HBox.setHgrow(currentRouteLabel, Priority.ALWAYS);
        currentRouteLabel.setAlignment(Pos.CENTER);

        routeNavPane.getChildren().addAll(prevRouteButton, currentRouteLabel, nextRouteButton);

        // Route information
        Label routeInfoLabel = new Label("Route Information:");
        routeInfoTextArea = new TextArea();
        routeInfoTextArea.setEditable(false);
        routeInfoTextArea.setPrefHeight(200);

        // Traffic simulation
        Label trafficLabel = new Label("Traffic Simulation:");
        Button simulateTrafficButton = new Button("Simulate Traffic Incident");
        simulateTrafficButton.setPrefWidth(340);
        simulateTrafficButton.setOnAction(e -> simulateTrafficIncident());

        // Add all components
        controlsPane.getChildren().addAll(
                title,
                startLabel, startLocationComboBox,
                endLabel, endLocationComboBox,
                findRouteButton,
                new Separator(),
                routeNavPane,
                routeInfoLabel, routeInfoTextArea,
                new Separator(),
                trafficLabel, simulateTrafficButton
        );

        return controlsPane;
    }

    private void initializeTrafficSystem() {
        cityNav = new TrafficManagementSystem();

        // Add major intersections/locations
        String[] locations = {
                "Downtown", "Airport", "University", "Mall",
                "Stadium", "Hospital", "Park", "Beach",
                "Industrial Area", "Residential Zone"
        };

        for (String location : locations) {
            cityNav.addIntersection(location);
        }

        // Set up location coordinates for visualization
        cityNav.setLocationCoordinates("Downtown", 400, 300);
        cityNav.setLocationCoordinates("Airport", 150, 150);
        cityNav.setLocationCoordinates("University", 500, 200);
        cityNav.setLocationCoordinates("Mall", 300, 400);
        cityNav.setLocationCoordinates("Stadium", 600, 450);
        cityNav.setLocationCoordinates("Hospital", 350, 250);
        cityNav.setLocationCoordinates("Park", 550, 350);
        cityNav.setLocationCoordinates("Beach", 700, 500);
        cityNav.setLocationCoordinates("Industrial Area", 250, 500);
        cityNav.setLocationCoordinates("Residential Zone", 450, 450);

        // Add roads with realistic distances and initial traffic conditions
        // Downtown connections
        cityNav.addRoad("Downtown", "University", 4, 7); // Heavy traffic to university
        cityNav.addRoad("Downtown", "Mall", 3, 8); // Congested shopping area
        cityNav.addRoad("Downtown", "Hospital", 2, 5); // Moderate traffic
        cityNav.addRoad("Downtown", "Industrial Area", 6, 3); // Less traffic

        // Airport connections
        cityNav.addRoad("Airport", "Downtown", 10, 6); // Busy route
        cityNav.addRoad("Airport", "Industrial Area", 8, 2); // Light traffic
        cityNav.addRoad("Airport", "Beach", 15, 4); // Tourist route

        // University connections
        cityNav.addRoad("University", "Residential Zone", 5, 6); // Students commuting
        cityNav.addRoad("University", "Park", 3, 3); // Nice route through park
        cityNav.addRoad("University", "Downtown", 4, 6); // Return path to Downtown

        // Mall connections
        cityNav.addRoad("Mall", "Residential Zone", 4, 5);
        cityNav.addRoad("Mall", "Stadium", 7, 2);
        cityNav.addRoad("Mall", "Downtown", 3, 7); // Return path to Downtown

        // Stadium connections
        cityNav.addRoad("Stadium", "Park", 4, 1); // Light traffic
        cityNav.addRoad("Stadium", "Beach", 9, 3);
        cityNav.addRoad("Stadium", "Mall", 7, 3); // Return path to Mall

        // Hospital connections
        cityNav.addRoad("Hospital", "University", 3, 4);
        cityNav.addRoad("Hospital", "Residential Zone", 5, 2);
        cityNav.addRoad("Hospital", "Downtown", 2, 6); // Return path to Downtown

        // Park connections
        cityNav.addRoad("Park", "Beach", 6, 3);
        cityNav.addRoad("Park", "Residential Zone", 4, 2);
        cityNav.addRoad("Park", "University", 3, 4); // Return path to University
        cityNav.addRoad("Park", "Stadium", 4, 2); // Return path to Stadium

        // Beach connections
        cityNav.addRoad("Beach", "Industrial Area", 12, 1); // Remote route
        cityNav.addRoad("Beach", "Park", 6, 4); // Return path to Park
        cityNav.addRoad("Beach", "Stadium", 9, 2); // Return path to Stadium

        // Industrial Area connections
        cityNav.addRoad("Industrial Area", "Residential Zone", 7, 4);
        cityNav.addRoad("Industrial Area", "Downtown", 6, 4); // Return path to Downtown
        cityNav.addRoad("Industrial Area", "Airport", 8, 3); // Return path to Airport
        cityNav.addRoad("Industrial Area", "Beach", 12, 2); // Return path to Beach

        // Residential Zone connections
        cityNav.addRoad("Residential Zone", "University", 5, 5); // Return path to University
        cityNav.addRoad("Residential Zone", "Mall", 4, 6); // Return path to Mall
        cityNav.addRoad("Residential Zone", "Hospital", 5, 3); // Return path to Hospital
        cityNav.addRoad("Residential Zone", "Park", 4, 3); // Return path to Park
        cityNav.addRoad("Residential Zone", "Industrial Area", 7, 5); // Return path to Industrial Area
    }

    private void drawMap(List<String> highlightRoute) {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();

        // Clear canvas
        gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Draw roads first (so they're behind the locations)
        for (String from : cityNav.getAllLocations()) {
            Pair<Double, Double> fromCoords = cityNav.getLocationCoordinates(from);

            for (Road road : cityNav.getCityMap().get(from)) {
                String to = road.destination;
                Pair<Double, Double> toCoords = cityNav.getLocationCoordinates(to);

                // Determine if this road segment is part of the highlighted route
                boolean isHighlighted = false;
                if (highlightRoute != null) {
                    for (int i = 0; i < highlightRoute.size() - 1; i++) {
                        if (highlightRoute.get(i).equals(from) && highlightRoute.get(i + 1).equals(to)) {
                            isHighlighted = true;
                            break;
                        }
                    }
                }

                // Draw road with color based on traffic level or highlight
                if (isHighlighted) {
                    // Highlighted route path
                    gc.setStroke(Color.BLUE);
                    gc.setLineWidth(4);
                } else {
                    // Normal road - color based on traffic level
                    Color roadColor = getTrafficColor(road.trafficLevel);
                    gc.setStroke(roadColor);
                    gc.setLineWidth(2);
                }

                // Draw the road
                gc.strokeLine(fromCoords.getKey(), fromCoords.getValue(),
                        toCoords.getKey(), toCoords.getValue());

                // Draw small direction arrow
                drawDirectionArrow(gc, fromCoords.getKey(), fromCoords.getValue(),
                        toCoords.getKey(), toCoords.getValue());

                // Add distance and traffic level info
                if (!isHighlighted) {
                    // Only show detailed info for non-highlighted roads to reduce clutter
                    double midX = (fromCoords.getKey() + toCoords.getKey()) / 2;
                    double midY = (fromCoords.getValue() + toCoords.getValue()) / 2;

                    gc.setFill(Color.DARKGRAY);
                    gc.fillText(road.distance + "km | " + road.trafficLevel + "/10", midX, midY);
                }
            }
        }

        // Draw locations (intersections)
        for (String location : cityNav.getAllLocations()) {
            Pair<Double, Double> coords = cityNav.getLocationCoordinates(location);

            // Determine if this location is part of the highlighted route
            boolean isHighlighted = (highlightRoute != null && highlightRoute.contains(location));

            // Draw location
            if (isHighlighted) {
                gc.setFill(Color.BLUE);
                gc.fillOval(coords.getKey() - 8, coords.getValue() - 8, 16, 16);
            } else {
                gc.setFill(Color.RED);
                gc.fillOval(coords.getKey() - 6, coords.getValue() - 6, 12, 12);
            }

            // Location name
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("System", FontWeight.BOLD, 12));
            gc.fillText(location, coords.getKey() + 10, coords.getValue());
        }
    }

    private void drawDirectionArrow(GraphicsContext gc, double x1, double y1, double x2, double y2) {
        // Calculate midpoint of the line, slightly closer to the destination
        double t = 0.6; // Position along the line (0.5 would be exactly midpoint)
        double midX = x1 + t * (x2 - x1);
        double midY = y1 + t * (y2 - y1);

        // Calculate direction vector and normalize it
        double dirX = x2 - x1;
        double dirY = y2 - y1;
        double length = Math.sqrt(dirX * dirX + dirY * dirY);
        dirX /= length;
        dirY /= length;

        // Calculate perpendicular vectors for arrow head
        double perpX = -dirY;
        double perpY = dirX;

        // Arrow dimensions
        double arrowSize = 6;

        // Draw the arrow head
        double[] arrowHeadX = {
                midX + dirX * arrowSize,
                midX - dirX * arrowSize / 2 + perpX * arrowSize / 2,
                midX - dirX * arrowSize / 2 - perpX * arrowSize / 2
        };

        double[] arrowHeadY = {
                midY + dirY * arrowSize,
                midY - dirY * arrowSize / 2 + perpY * arrowSize / 2,
                midY - dirY * arrowSize / 2 - perpY * arrowSize / 2
        };

        gc.setFill(gc.getStroke());
        gc.fillPolygon(arrowHeadX, arrowHeadY, 3);
    }

    private Color getTrafficColor(int trafficLevel) {
        // Traffic color gradient from green (low traffic) to red (high traffic)
        if (trafficLevel <= 3) {
            return Color.GREEN;
        } else if (trafficLevel <= 6) {
            return Color.ORANGE;
        } else {
            return Color.RED;
        }
    }

    private void findRoutes() {
        String start = startLocationComboBox.getValue();
        String end = endLocationComboBox.getValue();

        if (start == null || end == null) {
            showAlert("Please select both starting point and destination.");
            return;
        }

        currentRouteOptions = findAlternativeRoutes(cityNav, start, end);
        currentRouteIndex = 0;

        if (currentRouteOptions.isEmpty()) {
            showAlert("No direct route available between " + start + " and " + end);
            routeInfoTextArea.setText("No viable routes found between selected locations.");
            prevRouteButton.setDisable(true);
            nextRouteButton.setDisable(true);
            currentRouteLabel.setText("Route: 0/0");
            drawMap(null);
            return;
        }

        // Display the first route
        displayRoute(currentRouteIndex);
    }

    private void displayRoute(int index) {
        if (currentRouteOptions.isEmpty() || index < 0 || index >= currentRouteOptions.size()) {
            return;
        }

        RouteOption route = currentRouteOptions.get(index);
        currentDisplayedRoute = route.path;

        // Update UI
        StringBuilder routeInfo = new StringBuilder();
        routeInfo.append("Route ").append(index + 1).append(":\n");
        routeInfo.append(String.join(" → ", route.path)).append("\n\n");
        routeInfo.append("Estimated travel time: ").append(route.time).append(" minutes\n");

        if (index == 0) {
            routeInfo.append("\n--- RECOMMENDED ROUTE (Fastest) ---\n");
        } else {
            int difference = route.time - currentRouteOptions.get(0).time;
            routeInfo.append("\n(").append(difference).append(" minutes slower than the fastest route)\n");
        }

        routeInfoTextArea.setText(routeInfo.toString());
        currentRouteLabel.setText("Route: " + (index + 1) + "/" + currentRouteOptions.size());

        // Update navigation buttons
        prevRouteButton.setDisable(index == 0);
        nextRouteButton.setDisable(index == currentRouteOptions.size() - 1);

        // Redraw map with highlighted route
        drawMap(route.path);
    }

    private void showNextRoute() {
        if (currentRouteIndex < currentRouteOptions.size() - 1) {
            currentRouteIndex++;
            displayRoute(currentRouteIndex);
        }
    }

    private void showPreviousRoute() {
        if (currentRouteIndex > 0) {
            currentRouteIndex--;
            displayRoute(currentRouteIndex);
        }
    }

    private void simulateTrafficIncident() {
        // Check if we have routes
        if (currentRouteOptions.isEmpty() || currentDisplayedRoute.isEmpty()) {
            showAlert("Please find routes first before simulating traffic incidents.");
            return;
        }

        // Simulate a traffic incident on the current route
        Random random = new Random();

        // Select a random segment index (not the last one)
        int segmentIndex = random.nextInt(currentDisplayedRoute.size() - 1);
        String incident1 = currentDisplayedRoute.get(segmentIndex);
        String incident2 = currentDisplayedRoute.get(segmentIndex + 1);

        // Update traffic
        cityNav.updateTraffic(incident1, incident2, 10); // Heavy traffic/accident

        // Show incident notification
        showAlert("Traffic incident reported between " + incident1 + " and " + incident2);

        // Recalculate routes
        String start = startLocationComboBox.getValue();
        String end = endLocationComboBox.getValue();

        currentRouteOptions = findAlternativeRoutes(cityNav, start, end);
        currentRouteIndex = 0;

        // Display updated routes
        if (!currentRouteOptions.isEmpty()) {
            displayRoute(currentRouteIndex);

            // Add incident info to the text area
            String currentInfo = routeInfoTextArea.getText();
            routeInfoTextArea.setText(currentInfo + "\n\nNOTE: Routes recalculated after traffic incident between "
                    + incident1 + " and " + incident2);
        }
    }

    // Find multiple route options between two locations (from original code)
    private static List<RouteOption> findAlternativeRoutes(TrafficManagementSystem cityNav, String start, String end) {
        List<RouteOption> routes = new ArrayList<>();

        // Get initial fastest route
        List<String> fastestRoute = cityNav.findFastestRoute(start, end);
        if (!fastestRoute.isEmpty()) {
            int time = cityNav.getRouteTime(fastestRoute);
            routes.add(new RouteOption(new ArrayList<>(fastestRoute), time));

            // Make a deep copy of the city map to manipulate
            TrafficManagementSystem tempNav = cloneNavigator(cityNav);

            // Find alternative routes by temporarily modifying the graph
            for (int i = 0; i < Math.min(fastestRoute.size() - 1, 3); i++) {
                // Temporarily increase traffic on one segment of the route to force alternative
                String from = fastestRoute.get(i);
                String to = fastestRoute.get(i + 1);

                // Temporarily make this segment very congested
                tempNav.updateTraffic(from, to, 10);

                // Find alternative route
                List<String> altRoute = tempNav.findFastestRoute(start, end);
                if (!altRoute.isEmpty() && !isIdenticalRoute(routes, altRoute)) {
                    int altTime = tempNav.getRouteTime(altRoute);
                    routes.add(new RouteOption(altRoute, altTime));
                }

                // Reset the traffic to try another segment
                tempNav = cloneNavigator(cityNav);
            }
        }

        // Sort routes by travel time
        routes.sort(Comparator.comparingInt(r -> r.time));

        // Limit to top 3 alternative routes
        if (routes.size() > 3) {
            routes = routes.subList(0, 3);
        }

        return routes;
    }

    // Check if a route is already in our list (avoid duplicates)
    private static boolean isIdenticalRoute(List<RouteOption> routes, List<String> newRoute) {
        for (RouteOption route : routes) {
            if (route.path.equals(newRoute)) {
                return true;
            }
        }
        return false;
    }

    // Create a deep copy of the TrafficManagementSystem
    private static TrafficManagementSystem cloneNavigator(TrafficManagementSystem original) {
        TrafficManagementSystem clone = new TrafficManagementSystem();

        // Copy all intersections and their coordinates
        for (String location : original.getAllLocations()) {
            clone.addIntersection(location);

            // Copy coordinates if available
            Pair<Double, Double> coords = original.getLocationCoordinates(location);
            if (coords != null) {
                clone.setLocationCoordinates(location, coords.getKey(), coords.getValue());
            }
        }

        // Copy all roads with their current traffic conditions
        for (String from : original.getCityMap().keySet()) {
            for (Road road : original.getCityMap().get(from)) {
                clone.addRoad(from, road.destination, road.distance, road.trafficLevel);
            }
        }

        return clone;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Traffic Management System");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}