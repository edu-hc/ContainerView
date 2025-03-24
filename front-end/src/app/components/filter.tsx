import React, { useState } from 'react';
import { View, Text, TouchableOpacity, Modal, Pressable, Image } from 'react-native';

interface FilterOption {
  id: number;
  name: string;
}

const FilterButton: React.FC = () => {
  const [modalVisible, setModalVisible] = useState<boolean>(false);
  const [activeFilter, setActiveFilter] = useState<number | null>(null);
  
  const filterOptions: FilterOption[] = [
    { id: 1, name: 'Mais Recentes' },
    { id: 2, name: 'Mais Antigos' },
  ];
  
  const selectAndApplyFilter = (filterId: number): void => {
    // Se o mesmo filtro foi clicado, desmarque-o
    if (activeFilter === filterId) {
      setActiveFilter(null);
    } else {
      // Caso contrário, selecione o novo filtro
      setActiveFilter(filterId);
    }
    
    // Implementa a lógica para aplicar o filtro
    console.log('Filtro aplicado:', filterId);
    
    // Fecha o modal imediatamente após a seleção
    setModalVisible(false);
  };
  
  return (
    <View className="items-center p-3">
      {/* Botão de Filtro */}
      <TouchableOpacity
        onPress={() => setModalVisible(true)}
        className="flex-row items-center bg-blue-500 px-4 py-2 rounded-lg"
      >
        <Image
          source={require('./icons/filtro.png')}
          className="w-5 h-7"
          resizeMode="contain"
        />
      </TouchableOpacity>
      
      {/* Modal do Overlay (sem animação) */}
      <Modal
        animationType="fade"
        transparent={true}
        visible={modalVisible}
        onRequestClose={() => setModalVisible(false)}
      >
        <Pressable
          style={{ flex: 1, backgroundColor: 'rgba(0,0,0,0.5)' }}
          onPress={() => setModalVisible(false)}
        />
      </Modal>
      
      {/* Modal do Conteúdo (com animação slide) */}
      <Modal
        animationType="slide"
        transparent={true}
        visible={modalVisible}
        onRequestClose={() => setModalVisible(false)}
      >
        <View className="flex-1 justify-end bg-transparent" pointerEvents="box-none">
          <View className="bg-white rounded-t-lg p-6">
            <Text className="text-xl font-bold mb-4">Opções de Filtro</Text>
            
            {filterOptions.map((option) => (
              <TouchableOpacity
                key={option.id}
                onPress={() => selectAndApplyFilter(option.id)}
                className={`p-3 mb-2 border rounded-md ${
                  activeFilter === option.id ? 'bg-blue-100 border-blue-500' : 'border-gray-300'
                }`}
              >
                <Text className={activeFilter === option.id ? 'text-blue-500' : 'text-gray-700'}>
                  {option.name}
                </Text>
              </TouchableOpacity>
            ))}
            
            <View className="flex-row justify-center mt-4">
              <Pressable
                onPress={() => setModalVisible(false)}
                className="bg-gray-200 px-6 py-3 rounded-md"
              >
                <Text className="text-gray-700 font-medium">Cancelar</Text>
              </Pressable>
            </View>
          </View>
        </View>
      </Modal>
    </View>
  );
};

export default FilterButton;