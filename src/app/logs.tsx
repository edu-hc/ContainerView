import React, { useState, useRef, useEffect } from 'react';
import { View, Text, TouchableOpacity, ScrollView, SafeAreaView, Animated, TouchableWithoutFeedback, TextInput, FlatList, StyleSheet } from 'react-native';
import { Svg, Path } from 'react-native-svg';
import { cssInterop } from 'nativewind';
import sombra from "./images/style"
import users from "./results";
import ListItem from "./components/operations"
import FilterButton from './components/filter';
import { router } from 'expo-router';

// Interop para permitir o uso de classes Tailwind em componentes React Native
cssInterop(View, { className: 'style' });
cssInterop(Text, { className: 'style' });
cssInterop(TouchableOpacity, { className: 'style' });
cssInterop(ScrollView, { className: 'style' });
cssInterop(SafeAreaView, { className: 'style' });
cssInterop(Animated.View, { className: 'style' });
cssInterop(TouchableWithoutFeedback, { className: 'style' });

export default function Logs() {
  const [searchField, setSearchField] = useState<SearchField>('operacao');
  type SearchField = 'operacao' | 'container';
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
            item[searchField].toLowerCase().includes(searchText.toLowerCase())
        )
      );
    }
  }, [searchText, searchField]);

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
        <View className='w-full h-28 bg-indigo-400 shadow-lg' style={sombra.shadow}>
          <TouchableOpacity
            className="absolute p-2 mt-12 ml-3 z-10"
            onPress={toggleSidebar}
          >
            <Svg width={30} height={30} viewBox="0 0 20 20" fill="#000000">
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
              {/* Operações */}
              <TouchableOpacity className="flex-row items-center p-2 rounded-lg mb-2 bg-white hover:bg-slate-400">
                <KanbanIcon />
                <Text className="ml-3 text-base font-medium text-gray-900 flex-1">Operações</Text>
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
            placeholder={`Pesquise por ${searchField === 'operacao' ? 'operação' : 'container'}`}
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
          <FilterButton
            onFilterChange={setSearchField}
            currentFilterField={searchField}
          />
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
        {/* Botão Flutuante */}
        <TouchableOpacity style={styles.floatingButton}>
          <Text style={styles.floatingButtonText}>+</Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
}

// Icons Components (mesmo do exemplo anterior)
const KanbanIcon = () => (
  <Svg width={20} height={25} viewBox="3 0 20 20" fill="#000000">
    <Path
      fillRule="evenodd"
      d="M8 3a1 1 0 011-1h6a1 1 0 011 1h2a2 2 0 012 2v15a2 2 0 01-2 2H6a2 2 0 01-2-2V5a2 2 0 012-2h2zm6 1h-4v2H9a1 1 0 000 2h6a1 1 0 100-2h-1V4zm-3 8a1 1 0 011-1h3a1 1 0 110 2h-3a1 1 0 01-1-1zm-2-1a1 1 0 100 2h.01a1 1 0 100-2H9zm2 5a1 1 0 011-1h3a1 1 0 110 2h-3a1 1 0 01-1-1zm-2-1a1 1 0 100 2h.01a1 1 0 100-2H9z"
      clipRule="evenodd"
    />
  </Svg>
);

const SignUpIcon = () => (
  <Svg width={20} height={20} viewBox="0 0 20 20" fill="#000000">
    <Path d="M5 5V.13a2.96 2.96 0 0 0-1.293.749L.879 3.707A2.96 2.96 0 0 0 .13 5H5Z" />
    <Path d="M6.737 11.061a2.961 2.961 0 0 1 .81-1.515l6.117-6.116A4.839 4.839 0 0 1 16 2.141V2a1.97 1.97 0 0 0-1.933-2H7v5a2 2 0 0 1-2 2H0v11a1.969 1.969 0 0 0 1.933 2h12.134A1.97 1.97 0 0 0 16 18v-3.093l-1.546 1.546c-.413.413-.94.695-1.513.81l-3.4.679a2.947 2.947 0 0 1-1.85-.227 2.96 2.96 0 0 1-1.635-3.257l.681-3.397Z" />
    <Path d="M8.961 16a.93.93 0 0 0 .189-.019l3.4-.679a.961.961 0 0 0 .49-.263l6.118-6.117a2.884 2.884 0 0 0-4.079-4.078l-6.117 6.117a.96.96 0 0 0-.263.491l-.679 3.4A.961.961 0 0 0 8.961 16Zm7.477-9.8a.958.958 0 0 1 .68-.281.961.961 0 0 1 .682 1.644l-.315.315-1.36-1.36.313-.318Zm-5.911 5.911 4.236-4.236 1.359 1.359-4.236 4.237-1.7.339.341-1.699Z" />
  </Svg>
);

const styles = StyleSheet.create({
  floatingButton: {
    position: 'absolute',
    bottom: 20,
    right: 20,
    backgroundColor: '#4f46e5',
    width: 60,
    height: 60,
    borderRadius: 30,
    justifyContent: 'center',
    alignItems: 'center',
    elevation: 5,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.3,
    shadowRadius: 2,
  },
  floatingButtonText: {
    color: 'white',
    fontSize: 24,
  },
});