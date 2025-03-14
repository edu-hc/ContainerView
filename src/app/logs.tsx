import React, { useState, useRef, useEffect } from 'react';
import { View, Text, TouchableOpacity, ScrollView, SafeAreaView, StyleSheet, Animated, TouchableWithoutFeedback } from 'react-native';
import { Svg, Path } from 'react-native-svg';

// Since we're not using an actual tailwind library in this example,
// we'll use regular React Native styles to mimic the Tailwind classes

const Logs = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const translateX = useRef(new Animated.Value(-256)).current;
  const overlayOpacity = useRef(new Animated.Value(0)).current;
  const sidebarWidth = 256;

  const toggleSidebar = () => {
    // Start both animations together
    Animated.parallel([
      Animated.timing(translateX, {
        toValue: sidebarOpen ? -sidebarWidth : 0,
        duration: 300,
        useNativeDriver: true,
      }),
      Animated.timing(overlayOpacity, {
        toValue: sidebarOpen ? 0 : 1,
        duration: 300,
        useNativeDriver: true,
      })
    ]).start();
    
    setSidebarOpen(!sidebarOpen);
  };

  const closeSidebar = () => {
    if (sidebarOpen) {
      Animated.parallel([
        Animated.timing(translateX, {
          toValue: -sidebarWidth,
          duration: 300,
          useNativeDriver: true,
        }),
        Animated.timing(overlayOpacity, {
          toValue: 0,
          duration: 300,
          useNativeDriver: true,
        })
      ]).start();
      
      setSidebarOpen(false);
    }
  };

  // Reset animation values when component mounts
  useEffect(() => {
    translateX.setValue(sidebarOpen ? 0 : -sidebarWidth);
    overlayOpacity.setValue(sidebarOpen ? 1 : 0);
  }, []);

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.wrapper}>
        {/* Mobile Toggle Button */}
        <TouchableOpacity
          style={styles.toggleButton}
          onPress={toggleSidebar}
        >
          <Svg width={24} height={24} viewBox="0 0 20 20" fill="#6B7280">
            <Path
              d="M2 4.75A.75.75 0 012.75 4h14.5a.75.75 0 010 1.5H2.75A.75.75 0 012 4.75zm0 10.5a.75.75 0 01.75-.75h7.5a.75.75 0 010 1.5h-7.5a.75.75 0 01-.75-.75zM2 10a.75.75 0 01.75-.75h14.5a.75.75 0 010 1.5H2.75A.75.75 0 012 10z"
            />
          </Svg>
        </TouchableOpacity>

        {/* Animated Overlay */}
        <Animated.View 
          style={[
            styles.overlay,
            { opacity: overlayOpacity },
            // Hide the overlay completely when fully transparent
            { pointerEvents: sidebarOpen ? 'auto' : 'none' }
          ]}
        >
          <TouchableWithoutFeedback onPress={closeSidebar}>
            <View style={styles.overlayTouchable} />
          </TouchableWithoutFeedback>
        </Animated.View>

        {/* Sidebar */}
        <Animated.View
          style={[
            styles.sidebar,
            { transform: [{ translateX: translateX }] }
          ]}
        >
          <ScrollView style={styles.sidebarScroll}>
            <View style={styles.menuList}>
              {/* Dashboard */}
              <TouchableOpacity style={styles.menuItem}>
                <DashboardIcon />
                <Text style={styles.menuText}>Dashboard</Text>
              </TouchableOpacity>

              {/* Kanban */}
              <TouchableOpacity style={styles.menuItem}>
                <KanbanIcon />
                <Text style={styles.menuText}>Kanban</Text>
                <View style={styles.proBadge}>
                  <Text style={styles.badgeText}>Pro</Text>
                </View>
              </TouchableOpacity>

              {/* Inbox */}
              <TouchableOpacity style={styles.menuItem}>
                <InboxIcon />
                <Text style={styles.menuText}>Inbox</Text>
                <View style={styles.notificationBadge}>
                  <Text style={styles.notificationText}>3</Text>
                </View>
              </TouchableOpacity>

              {/* Users */}
              <TouchableOpacity style={styles.menuItem}>
                <UsersIcon />
                <Text style={styles.menuText}>Users</Text>
              </TouchableOpacity>

              {/* Products */}
              <TouchableOpacity style={styles.menuItem}>
                <ProductsIcon />
                <Text style={styles.menuText}>Products</Text>
              </TouchableOpacity>

              {/* Sign In */}
              <TouchableOpacity style={styles.menuItem}>
                <SignInIcon />
                <Text style={styles.menuText}>Sign In</Text>
              </TouchableOpacity>

              {/* Sign Up */}
              <TouchableOpacity style={styles.menuItem}>
                <SignUpIcon />
                <Text style={styles.menuText}>Sign Up</Text>
              </TouchableOpacity>
            </View>
          </ScrollView>
        </Animated.View>

        {/* Main Content */}
        <View style={styles.content}>
          
        </View>
      </View>
    </SafeAreaView>
  );
};

// Icons Components
const DashboardIcon = () => (
  <Svg width={20} height={20} viewBox="0 0 22 21" fill="#6B7280">
    <Path d="M16.975 11H10V4.025a1 1 0 0 0-1.066-.998 8.5 8.5 0 1 0 9.039 9.039.999.999 0 0 0-1-1.066h.002Z" />
    <Path d="M12.5 0c-.157 0-.311.01-.565.027A1 1 0 0 0 11 1.02V10h8.975a1 1 0 0 0 1-.935c.013-.188.028-.374.028-.565A8.51 8.51 0 0 0 12.5 0Z" />
  </Svg>
);

const KanbanIcon = () => (
  <Svg width={20} height={20} viewBox="0 0 18 18" fill="#6B7280">
    <Path d="M6.143 0H1.857A1.857 1.857 0 0 0 0 1.857v4.286C0 7.169.831 8 1.857 8h4.286A1.857 1.857 0 0 0 8 6.143V1.857A1.857 1.857 0 0 0 6.143 0Zm10 0h-4.286A1.857 1.857 0 0 0 10 1.857v4.286C10 7.169 10.831 8 11.857 8h4.286A1.857 1.857 0 0 0 18 6.143V1.857A1.857 1.857 0 0 0 16.143 0Zm-10 10H1.857A1.857 1.857 0 0 0 0 11.857v4.286C0 17.169.831 18 1.857 18h4.286A1.857 1.857 0 0 0 8 16.143v-4.286A1.857 1.857 0 0 0 6.143 10Zm10 0h-4.286A1.857 1.857 0 0 0 10 11.857v4.286c0 1.026.831 1.857 1.857 1.857h4.286A1.857 1.857 0 0 0 18 16.143v-4.286A1.857 1.857 0 0 0 16.143 10Z" />
  </Svg>
);

const InboxIcon = () => (
  <Svg width={20} height={20} viewBox="0 0 20 20" fill="#6B7280">
    <Path d="m17.418 3.623-.018-.008a6.713 6.713 0 0 0-2.4-.569V2h1a1 1 0 1 0 0-2h-2a1 1 0 0 0-1 1v2H9.89A6.977 6.977 0 0 1 12 8v5h-2V8A5 5 0 1 0 0 8v6a1 1 0 0 0 1 1h8v4a1 1 0 0 0 1 1h2a1 1 0 0 0 1-1v-4h6a1 1 0 0 0 1-1V8a5 5 0 0 0-2.582-4.377ZM6 12H4a1 1 0 0 1 0-2h2a1 1 0 0 1 0 2Z" />
  </Svg>
);

const UsersIcon = () => (
  <Svg width={20} height={20} viewBox="0 0 20 18" fill="#6B7280">
    <Path d="M14 2a3.963 3.963 0 0 0-1.4.267 6.439 6.439 0 0 1-1.331 6.638A4 4 0 1 0 14 2Zm1 9h-1.264A6.957 6.957 0 0 1 15 15v2a2.97 2.97 0 0 1-.184 1H19a1 1 0 0 0 1-1v-1a5.006 5.006 0 0 0-5-5ZM6.5 9a4.5 4.5 0 1 0 0-9 4.5 4.5 0 0 0 0 9ZM8 10H5a5.006 5.006 0 0 0-5 5v2a1 1 0 0 0 1 1h11a1 1 0 0 0 1-1v-2a5.006 5.006 0 0 0-5-5Z" />
  </Svg>
);

const ProductsIcon = () => (
  <Svg width={20} height={20} viewBox="0 0 18 20" fill="#6B7280">
    <Path d="M17 5.923A1 1 0 0 0 16 5h-3V4a4 4 0 1 0-8 0v1H2a1 1 0 0 0-1 .923L.086 17.846A2 2 0 0 0 2.08 20h13.84a2 2 0 0 0 1.994-2.153L17 5.923ZM7 9a1 1 0 0 1-2 0V7h2v2Zm0-5a2 2 0 1 1 4 0v1H7V4Zm6 5a1 1 0 1 1-2 0V7h2v2Z" />
  </Svg>
);

const SignInIcon = () => (
  <Svg width={20} height={20} viewBox="0 0 18 16" fill="none">
    <Path
      stroke="#6B7280"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
      d="M1 8h11m0 0L8 4m4 4-4 4m4-11h3a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2h-3"
    />
  </Svg>
);

const SignUpIcon = () => (
  <Svg width={20} height={20} viewBox="0 0 20 20" fill="#6B7280">
    <Path d="M5 5V.13a2.96 2.96 0 0 0-1.293.749L.879 3.707A2.96 2.96 0 0 0 .13 5H5Z" />
    <Path d="M6.737 11.061a2.961 2.961 0 0 1 .81-1.515l6.117-6.116A4.839 4.839 0 0 1 16 2.141V2a1.97 1.97 0 0 0-1.933-2H7v5a2 2 0 0 1-2 2H0v11a1.969 1.969 0 0 0 1.933 2h12.134A1.97 1.97 0 0 0 16 18v-3.093l-1.546 1.546c-.413.413-.94.695-1.513.81l-3.4.679a2.947 2.947 0 0 1-1.85-.227 2.96 2.96 0 0 1-1.635-3.257l.681-3.397Z" />
    <Path d="M8.961 16a.93.93 0 0 0 .189-.019l3.4-.679a.961.961 0 0 0 .49-.263l6.118-6.117a2.884 2.884 0 0 0-4.079-4.078l-6.117 6.117a.96.96 0 0 0-.263.491l-.679 3.4A.961.961 0 0 0 8.961 16Zm7.477-9.8a.958.958 0 0 1 .68-.281.961.961 0 0 1 .682 1.644l-.315.315-1.36-1.36.313-.318Zm-5.911 5.911 4.236-4.236 1.359 1.359-4.236 4.237-1.7.339.341-1.699Z" />
  </Svg>
);

// Styles
const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#ffffff'
  },
  wrapper: {
    flex: 1,
    flexDirection: 'row'
  },
  toggleButton: {
    position: 'absolute',
    padding: 8,
    marginTop: 8,
    marginLeft: 12,
    zIndex: 10
  },
  overlay: {
    position: 'absolute',
    width: '100%',
    height: '100%',
    backgroundColor: 'rgba(0,0,0,0.3)',
    zIndex: 30,
  },
  overlayTouchable: {
    width: '100%',
    height: '100%',
  },
  sidebar: {
    position: 'absolute',
    left: 0,
    width: 256,
    height: '100%',
    backgroundColor: '#f9fafb',
    zIndex: 40,
    shadowColor: '#000',
    shadowOffset: { width: 2, height: 0 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 5,
  },
  sidebarScroll: {
    height: '100%',
    paddingHorizontal: 12,
    paddingVertical: 16
  },
  menuList: {
    marginVertical: 8
  },
  menuItem: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 8,
    borderRadius: 8,
    marginBottom: 8
  },
  menuText: {
    marginLeft: 12,
    fontSize: 16,
    fontWeight: '500',
    color: '#111827',
    flex: 1
  },
  proBadge: {
    paddingHorizontal: 8,
    paddingVertical: 2,
    marginLeft: 12,
    backgroundColor: '#f3f4f6',
    borderRadius: 9999,
  },
  badgeText: {
    fontSize: 12,
    fontWeight: '500',
    color: '#1f2937'
  },
  notificationBadge: {
    width: 24,
    height: 24,
    borderRadius: 9999,
    backgroundColor: '#dbeafe',
    alignItems: 'center',
    justifyContent: 'center',
    marginLeft: 12
  },
  notificationText: {
    fontSize: 12,
    fontWeight: '500',
    color: '#1e40af'
  },
  content: {
    flex: 1,
    padding: 16,
    marginLeft: 0
  }
});

export default Logs;