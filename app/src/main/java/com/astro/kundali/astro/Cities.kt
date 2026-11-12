package com.astro.kundali.astro

object Cities {
    val all =
        """
Delhi|28.6139|77.2090|Asia/Kolkata
Mumbai|19.0760|72.8777|Asia/Kolkata
Kolkata|22.5726|88.3639|Asia/Kolkata
Chennai|13.0827|80.2707|Asia/Kolkata
Bengaluru|12.9716|77.5946|Asia/Kolkata
Hyderabad|17.3850|78.4867|Asia/Kolkata
Ahmedabad|23.0225|72.5714|Asia/Kolkata
Pune|18.5204|73.8567|Asia/Kolkata
Jaipur|26.9124|75.7873|Asia/Kolkata
Lucknow|26.8467|80.9462|Asia/Kolkata
Chandigarh|30.7333|76.7794|Asia/Kolkata
Amritsar|31.6340|74.8723|Asia/Kolkata
Ludhiana|30.9010|75.8573|Asia/Kolkata
Jalandhar|31.3260|75.5762|Asia/Kolkata
Patna|25.5941|85.1376|Asia/Kolkata
Bhopal|23.2599|77.4126|Asia/Kolkata
Indore|22.7196|75.8577|Asia/Kolkata
Nagpur|21.1458|79.0882|Asia/Kolkata
Surat|21.1702|72.8311|Asia/Kolkata
Vadodara|22.3072|73.1812|Asia/Kolkata
Varanasi|25.3176|82.9739|Asia/Kolkata
Prayagraj|25.4358|81.8463|Asia/Kolkata
Agra|27.1767|78.0081|Asia/Kolkata
Kanpur|26.4499|80.3319|Asia/Kolkata
Dehradun|30.3165|78.0322|Asia/Kolkata
Shimla|31.1048|77.1734|Asia/Kolkata
Srinagar|34.0837|74.7973|Asia/Kolkata
Jammu|32.7266|74.8570|Asia/Kolkata
Guwahati|26.1445|91.7362|Asia/Kolkata
Bhubaneswar|20.2961|85.8245|Asia/Kolkata
Ranchi|23.3441|85.3096|Asia/Kolkata
Raipur|21.2514|81.6296|Asia/Kolkata
Kochi|9.9312|76.2673|Asia/Kolkata
Thiruvananthapuram|8.5241|76.9366|Asia/Kolkata
Coimbatore|11.0168|76.9558|Asia/Kolkata
Madurai|9.9252|78.1198|Asia/Kolkata
Visakhapatnam|17.6868|83.2185|Asia/Kolkata
Vijayawada|16.5062|80.6480|Asia/Kolkata
Mysuru|12.2958|76.6394|Asia/Kolkata
Mangaluru|12.9141|74.8560|Asia/Kolkata
Panaji|15.4909|73.8278|Asia/Kolkata
Udaipur|24.5854|73.7125|Asia/Kolkata
Jodhpur|26.2389|73.0243|Asia/Kolkata
Toronto|43.6532|-79.3832|America/Toronto
Vancouver|49.2827|-123.1207|America/Vancouver
Calgary|51.0447|-114.0719|America/Edmonton
Montreal|45.5017|-73.5673|America/Toronto
Brampton|43.7315|-79.7624|America/Toronto
Surrey|49.1913|-122.8490|America/Vancouver
New York|40.7128|-74.0060|America/New_York
Los Angeles|34.0522|-118.2437|America/Los_Angeles
Chicago|41.8781|-87.6298|America/Chicago
Houston|29.7604|-95.3698|America/Chicago
San Francisco|37.7749|-122.4194|America/Los_Angeles
Seattle|47.6062|-122.3321|America/Los_Angeles
London|51.5074|-0.1278|Europe/London
Birmingham|52.4862|-1.8904|Europe/London
Manchester|53.4808|-2.2426|Europe/London
Edinburgh|55.9533|-3.1883|Europe/London
Dubai|25.2048|55.2708|Asia/Dubai
Abu Dhabi|24.4539|54.3773|Asia/Dubai
Singapore|1.3521|103.8198|Asia/Singapore
Kathmandu|27.7172|85.3240|Asia/Kathmandu
Pokhara|28.2096|83.9856|Asia/Kathmandu
Colombo|6.9271|79.8612|Asia/Colombo
Kandy|7.2906|80.6337|Asia/Colombo
Dhaka|23.8103|90.4125|Asia/Dhaka
Chattogram|22.3569|91.7832|Asia/Dhaka
Karachi|24.8607|67.0011|Asia/Karachi
Lahore|31.5204|74.3587|Asia/Karachi
Islamabad|33.6844|73.0479|Asia/Karachi
Sydney|-33.8688|151.2093|Australia/Sydney
Melbourne|-37.8136|144.9631|Australia/Melbourne
Brisbane|-27.4698|153.0251|Australia/Brisbane
Perth|-31.9523|115.8613|Australia/Perth
Auckland|-36.8509|174.7645|Pacific/Auckland
Wellington|-41.2865|174.7762|Pacific/Auckland
"""
            .trimIndent()
            .lines()
            .map {
                val a = it.split('|')
                Place(a[0], a[1].toDouble(), a[2].toDouble(), a[3])
            }
    val sample =
        Birth(
            "India",
            java.time.LocalDate.of(1947, 8, 15),
            java.time.LocalTime.MIDNIGHT,
            all.first(),
        )
}
