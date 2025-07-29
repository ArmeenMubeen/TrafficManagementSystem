import java.util.*;

public class TrafficManagementSystem {
    // Class to represent a Road
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

    // Helper class to store route information
    static class RouteOption {
        List<String> path;
        int time;

        public RouteOption(List<String> path, int time) {
            this.path = path;
            this.time = time;
        }
    }

    // Map to represent the city road network
    private Map<String, List<Road>> cityMap;

    public TrafficManagementSystem() {
        cityMap = new HashMap<>();
    }

    // Add a new intersection/location to the map
    public void addIntersection(String location) {
        if (!cityMap.containsKey(location)) {
            cityMap.put(location, new ArrayList<>());
        }
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

    public static void main(String[] args) {
        // Create a more complex city with realistic intersections
        TrafficManagementSystem cityNav = new TrafficManagementSystem();

        // Add major intersections/locations
        String[] locations = {
                "Downtown", "Airport", "University", "Mall",
                "Stadium", "Hospital", "Park", "Beach",
                "Industrial Area", "Residential Zone"
        };

        for (String location : locations) {
            cityNav.addIntersection(location);
        }

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

        // Demonstrate navigation
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to Traffic Management System");
        System.out.println("Available locations:");
        for (int i = 0; i < locations.length; i++) {
            System.out.println((i + 1) + ". " + locations[i]);
        }

        try {
            // Get user input for source and destination
            System.out.print("\nEnter starting point number (1-" + locations.length + "): ");
            int startIdx = scanner.nextInt() - 1;

            System.out.print("Enter destination number (1-" + locations.length + "): ");
            int endIdx = scanner.nextInt() - 1;

            if (startIdx < 0 || startIdx >= locations.length ||
                    endIdx < 0 || endIdx >= locations.length) {
                System.out.println("Invalid location selection.");
                return;
            }

            String start = locations[startIdx];
            String end = locations[endIdx];

            // Find multiple routes by temporarily removing edges
            List<RouteOption> routes = findAlternativeRoutes(cityNav, start, end);

            if (routes.isEmpty()) {
                System.out.println("\nNo direct route available between " + start + " and " + end);

                // Try to find any possible route by adding a temporary direct connection
                System.out.println("Calculating alternative paths using connections through other locations...");

                // Find all reachable locations from start
                Set<String> reachableFromStart = findReachableLocations(cityNav, start);

                // Find all locations that can reach end
                Set<String> canReachEnd = findLocationsReachingTo(cityNav, end);

                // Find intersection - locations that can form a bridge
                reachableFromStart.retainAll(canReachEnd);

                if (!reachableFromStart.isEmpty()) {
                    System.out.println("Found possible connection through intermediate locations.");
                    List<String> bridgingPath = new ArrayList<>();
                    bridgingPath.add(start);

                    // Just take the first connecting location
                    String connector = reachableFromStart.iterator().next();

                    // Find path from start to connector
                    List<String> firstLeg = cityNav.findFastestRoute(start, connector);
                    // Find path from connector to end
                    List<String> secondLeg = cityNav.findFastestRoute(connector, end);

                    // Combine paths (remove duplicate connector)
                    firstLeg.remove(firstLeg.size() - 1);
                    bridgingPath.addAll(firstLeg);
                    bridgingPath.addAll(secondLeg);

                    int pathTime = cityNav.getRouteTime(bridgingPath);
                    System.out.println("\nPossible Route Found:");
                    System.out.println(String.join(" → ", bridgingPath));
                    System.out.println("Estimated travel time: " + pathTime + " minutes");
                    System.out.println("Note: This is the only viable route found.");
                } else {

                    System.out.println("Unfortunately, there is no viable path between " + start + " and " + end +
                            " with the current road network.");
                    System.out.println("Please try different locations or check back later.");
                }
                return;
            }

            // Display routes sorted by travel time
            System.out.println("\nFound " + routes.size() + " possible routes from " + start + " to " + end + ":");

            for (int i = 0; i < routes.size(); i++) {
                RouteOption route = routes.get(i);
                System.out.println("\nRoute Option " + (i + 1) + ":");
                System.out.println(String.join(" => ", route.path));
                System.out.println("Estimated travel time: " + route.time + " minutes");

                if (i == 0) {
                    System.out.println(" ---RECOMMENDED ROUTE (Fastest)--- ");
                } else {
                    int difference = route.time - routes.get(0).time;
                    System.out.println("(" + difference + " minutes slower than the fastest route)");
                }
            }

            // Simulate traffic update
            System.out.println("\nTraffic update detected! Recalculating...");

            // Update traffic conditions on some roads from the fastest route
            List<String> fastestPath = routes.get(0).path;
            Random random = new Random();

            // FIX: Select adjacent locations to ensure they're directly connected
            // Select a random segment index (not the last one)
            int segmentIndex = random.nextInt(fastestPath.size() - 1);
            String incident1 = fastestPath.get(segmentIndex);
            String incident2 = fastestPath.get(segmentIndex + 1);

            // Now we're guaranteed to have two locations that are connected by a direct road
            cityNav.updateTraffic(incident1, incident2, 10); // Heavy traffic/accident
            System.out.println("Traffic incident reported between " + incident1 + " and " + incident2);

            // Recalculate all routes
            List<RouteOption> newRoutes = findAlternativeRoutes(cityNav, start, end);

            // Display updated routes
            System.out.println("\nUpdated routes after traffic incident:");

            for (int i = 0; i < newRoutes.size(); i++) {
                RouteOption route = newRoutes.get(i);
                System.out.println("\nRoute Option " + (i + 1) + ":");
                System.out.println(String.join(" => ", route.path));
                System.out.println("Estimated travel time: " + route.time + " minutes");

                if (i == 0) {
                    System.out.println(" ---NEW RECOMMENDED ROUTE--- ");

                    // Compare with original fastest route
                    if (route.time > routes.get(0).time) {
                        System.out.println("Note: Travel will take " + (route.time - routes.get(0).time) +
                                " minutes longer due to traffic conditions.");
                    } else if (route.time < routes.get(0).time) {
                        System.out.println("Good news! Your new route is " + (routes.get(0).time - route.time) +
                                " minutes faster than the original route.");
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    // Find multiple route options between two locations
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
        // This is a simplified clone that creates a new instance with the same
        // connections
        TrafficManagementSystem clone = new TrafficManagementSystem();

        // We use reflection to get access to the cityMap field
        try {
            java.lang.reflect.Field cityMapField = TrafficManagementSystem.class.getDeclaredField("cityMap");
            cityMapField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<String, List<TrafficManagementSystem.Road>> originalMap = (Map<String, List<TrafficManagementSystem.Road>>) cityMapField
                    .get(original);

            // Add all intersections first
            for (String location : originalMap.keySet()) {
                clone.addIntersection(location);
            }

            // Add all roads with their current traffic conditions
            for (Map.Entry<String, List<TrafficManagementSystem.Road>> entry : originalMap.entrySet()) {
                String from = entry.getKey();
                for (TrafficManagementSystem.Road road : entry.getValue()) {
                    clone.addRoad(from, road.destination, road.distance, road.trafficLevel);
                }
            }

        } catch (Exception e) {
            System.out.println("Error cloning navigator: " + e.getMessage());
        }

        return clone;
    }

    // Find all locations reachable from the starting point
    private static Set<String> findReachableLocations(TrafficManagementSystem cityNav, String start) {
        Set<String> reachable = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            String current = queue.poll();

            if (reachable.contains(current)) {
                continue;
            }

            reachable.add(current);

            // Get all direct connections from current location
            List<String> connections = getDirectConnections(cityNav, current);
            for (String next : connections) {
                if (!reachable.contains(next)) {
                    queue.add(next);
                }
            }
        }

        return reachable;
    }

    // Find all locations that can reach the destination
    private static Set<String> findLocationsReachingTo(TrafficManagementSystem cityNav, String end) {
        // This would require reversing the graph, but for simplicity,
        // we'll use our existing method and check for each location
        Set<String> canReach = new HashSet<>();

        try {
            java.lang.reflect.Field cityMapField = TrafficManagementSystem.class.getDeclaredField("cityMap");
            cityMapField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<String, List<TrafficManagementSystem.Road>> cityMap = (Map<String, List<TrafficManagementSystem.Road>>) cityMapField
                    .get(cityNav);

            for (String location : cityMap.keySet()) {
                List<String> path = cityNav.findFastestRoute(location, end);
                if (!path.isEmpty()) {
                    canReach.add(location);
                }
            }

        } catch (Exception e) {
            System.out.println("Error accessing city map: " + e.getMessage());
        }

        return canReach;
    }

    // Get direct connections from a location
    private static List<String> getDirectConnections(TrafficManagementSystem cityNav, String location) {
        List<String> connections = new ArrayList<>();

        try {
            java.lang.reflect.Field cityMapField = TrafficManagementSystem.class.getDeclaredField("cityMap");
            cityMapField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<String, List<TrafficManagementSystem.Road>> cityMap = (Map<String, List<TrafficManagementSystem.Road>>) cityMapField
                    .get(cityNav);

            List<TrafficManagementSystem.Road> roads = cityMap.get(location);
            if (roads != null) {
                for (TrafficManagementSystem.Road road : roads) {
                    connections.add(road.destination);
                }
            }

        } catch (Exception e) {
            System.out.println("Error accessing city map: " + e.getMessage());
        }

        return connections;
    }
}