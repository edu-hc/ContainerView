import React, { useState, useRef, useEffect } from 'react';
import { View, Text, TouchableOpacity, ScrollView, SafeAreaView, Animated, TouchableWithoutFeedback, TextInput, FlatList} from 'react-native';
import { Svg, Path } from 'react-native-svg';
import { cssInterop } from 'nativewind';
import { StyleSheet } from "react-native"
import sombra from "./images/style"
import users from "./results";
import ListItem from "./components/operations"
import FilterButton from './components/filter';

// Interop para permitir o uso de classes Tailwind em componentes React Native
cssInterop(View, { className: 'style' });
cssInterop(Text, { className: 'style' });
cssInterop(TouchableOpacity, { className: 'style' });
cssInterop(ScrollView, { className: 'style' });
cssInterop(SafeAreaView, { className: 'style' });
cssInterop(Animated.View, { className: 'style' });
cssInterop(TouchableWithoutFeedback, { className: 'style' });

export default function Logs() {
  
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const translateX = useRef(new Animated.Value(-256)).current;
  const overlayOpacity = useRef(new Animated.Value(0)).current;
  const sidebarWidth = 256;
  const [searchText, setSearchText] = useState('');
  const [list, setList] = useState(users);

  useEffect(() => {
    if (searchText === '') {
      setList(users);
    } else {
      setList(
        users.filter(
          (item) =>
            item.operacao.toLowerCase().indexOf(searchText.toLowerCase()) > -1
        )
      );
    }
    console.log(list); // Verifique os dados do FlatList
  }, [searchText]);

  const toggleSidebar = () => {
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
      }),
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
        }),
      ]).start();

      setSidebarOpen(false);
    }
  };

  useEffect(() => {
    translateX.setValue(sidebarOpen ? 0 : -sidebarWidth);
    overlayOpacity.setValue(sidebarOpen ? 1 : 0);
  }, []);

  return (
    <SafeAreaView className="flex-1 bg-white">
      <View className="flex-1 flex-col">
        {/* Mobile Toggle Button */}
        <View className='w-full h-28 bg-indigo-300 shadow-lg' style={sombra.shadow}>
          <TouchableOpacity
            className="absolute p-2 mt-12 ml-3 z-10"
            onPress={toggleSidebar}
          >
            <Svg width={30} height={30} viewBox="0 0 20 20" fill="#6B7280">
              <Path d="M2 4.75A.75.75 0 012.75 4h14.5a.75.75 0 010 1.5H2.75A.75.75 0 012 4.75zm0 10.5a.75.75 0 01.75-.75h7.5a.75.75 0 010 1.5h-7.5a.75.75 0 01-.75-.75zM2 10a.75.75 0 01.75-.75h14.5a.75.75 0 010 1.5H2.75A.75.75 0 012 10z" />
            </Svg>
          </TouchableOpacity>
        </View>
        {/* Animated Overlay */}
        <Animated.View
          className="absolute w-full h-full bg-black/30 z-30"
          style={{ opacity: overlayOpacity, pointerEvents: sidebarOpen ? 'auto' : 'none' }}
        >
          <TouchableWithoutFeedback onPress={closeSidebar}>
            <View className="w-full h-full" />
          </TouchableWithoutFeedback>
        </Animated.View>

        {/* Sidebar */}
        <Animated.View
          className="absolute left-0 w-64 h-full bg-gray-50 z-40 shadow-lg mt-7"
          style={{ transform: [{ translateX: translateX }] }}
        >
          <ScrollView className="h-full px-3 py-4">
            <View className="my-2">
              {/* Dashboard */}
              <TouchableOpacity className="flex-row items-center p-2 rounded-lg mb-2">
                <DashboardIcon />
                <Text className="ml-3 text-base font-medium text-gray-900 flex-1">Dashboard</Text>
              </TouchableOpacity>

              {/* Kanban */}
              <TouchableOpacity className="flex-row items-center p-2 rounded-lg mb-2 bg-white hover:bg-slate-400">
                <KanbanIcon />
                <Text className="ml-3 text-base font-medium text-gray-900 flex-1">Kanban</Text>
                <View className="px-2 py-1 ml-3 bg-gray-100 rounded-full">
                  <Text className="text-sm font-medium text-gray-700">Pro</Text>
                </View>
              </TouchableOpacity>

              {/* Inbox */}
              <TouchableOpacity className="flex-row items-center p-2 rounded-lg mb-2">
                <InboxIcon />
                <Text className="ml-3 text-base font-medium text-gray-900 flex-1">Inbox</Text>
                <View className="w-6 h-6 rounded-full bg-blue-100 items-center justify-center ml-3">
                  <Text className="text-sm font-medium text-blue-800">3</Text>
                </View>
              </TouchableOpacity>

              {/* Users */}
              <TouchableOpacity className="flex-row items-center p-2 rounded-lg mb-2">
                <UsersIcon />
                <Text className="ml-3 text-base font-medium text-gray-900 flex-1">Users</Text>
              </TouchableOpacity>

              {/* Products */}
              <TouchableOpacity className="flex-row items-center p-2 rounded-lg mb-2">
                <ProductsIcon />
                <Text className="ml-3 text-base font-medium text-gray-900 flex-1">Products</Text>
              </TouchableOpacity>

              {/* Sign In */}
              <TouchableOpacity className="flex-row items-center p-2 rounded-lg mb-2">
                <SignInIcon />
                <Text className="ml-3 text-base font-medium text-gray-900 flex-1">Sign In</Text>
              </TouchableOpacity>

              {/* Sign Up */}
              <TouchableOpacity className="flex-row items-center p-2 rounded-lg mb-2">
                <SignUpIcon />
                <Text className="ml-3 text-base font-medium text-gray-900 flex-1">Sign Up</Text>
              </TouchableOpacity>
            </View>
          </ScrollView>
        </Animated.View>
        <View className='mt-6 bg-white w-full flex-row'>
        <TextInput
            placeholder="Pesquise uma operação"
            placeholderTextColor="#888"
            value={searchText}
            onChangeText={(t) => setSearchText(t)}
            className="bg-gray-50 border border-gray-300 text-gray-900 rounded-lg px-3 py-2.5 w-80 mb-6 mt-3 ml-7"
            style={{
              shadowColor: "#000",
              shadowOffset: { width: 0, height: 1 },
              shadowOpacity: 0.1,
              shadowRadius: 2,
              elevation: 2,
            }}
          />
          <FilterButton />
        </View>
        <View className=' bg-white w-full flex-1 items-center'>
          <FlatList
            data={list}
            renderItem={({ item }) => <ListItem data={item} />}
            keyExtractor={(item) => item.operacao.toString()}
            className="flex-1"
            contentContainerStyle={{ paddingBottom: 20 }}
            showsVerticalScrollIndicator={false}
            ItemSeparatorComponent={() => <View style={{ height: 8 }} />}
            ListEmptyComponent={() => (
              <View className="items-center justify-center p-8">
                <Text className="text-gray-500 text-base">Nenhum resultado encontrado</Text>
              </View>
            )}
          />
        </View>
      </View>
    </SafeAreaView>
  );
}

// Icons Components (mesmo do exemplo anterior)
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
