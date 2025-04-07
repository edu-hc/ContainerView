import React, { useState, useRef, useEffect } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  Modal,
  Pressable,
  Image,
  StyleSheet,
  Animated,
  Easing,
  PanResponder,
  Dimensions,
} from 'react-native';

// Tipos
type SearchField = 'operacao' | 'container';

interface FilterOption {
  id: number;
  name: string;
  field: SearchField;
}

interface FilterButtonProps {
  onFilterChange: (field: SearchField) => void;
  currentFilterField: SearchField;
}

const FilterButton: React.FC<FilterButtonProps> = ({
  onFilterChange,
  currentFilterField,
}) => {
  const [modalVisible, setModalVisible] = useState<boolean>(false);
  const screenHeight = Dimensions.get('window').height;
  const modalPosition = useRef(new Animated.Value(screenHeight)).current;
  const backdropOpacity = useRef(new Animated.Value(0)).current;
  const modalOpacity = useRef(new Animated.Value(0)).current; // Nova animação de opacidade
  const filterOptions: FilterOption[] = [
    { id: 1, name: 'Pesquisar por operação', field: 'operacao' },
    { id: 2, name: 'Pesquisar por container', field: 'container' },
  ];

  useEffect(() => {
    if (modalVisible) {
      showModal();
    }
  }, [modalVisible]);

  const showModal = () => {
    modalPosition.setValue(screenHeight);
    modalOpacity.setValue(0); // Começa invisível
    backdropOpacity.setValue(0);

    Animated.parallel([
      Animated.timing(backdropOpacity, {
        toValue: 1,
        duration: 350,
        useNativeDriver: true,
        easing: Easing.out(Easing.cubic),
      }),
      Animated.timing(modalOpacity, {
        toValue: 1, // Torna visível
        duration: 350,
        useNativeDriver: true,
        easing: Easing.out(Easing.cubic),
      }),
      Animated.spring(modalPosition, {
        toValue: 0,
        friction: 8,
        tension: 45,
        useNativeDriver: true,
      }),
    ]).start();
  };

  const hideModal = (callback?: () => void) => {
    Animated.parallel([
      Animated.timing(backdropOpacity, {
        toValue: 0,
        duration: 250,
        useNativeDriver: true,
        easing: Easing.in(Easing.cubic),
      }),
      Animated.timing(modalOpacity, {
        toValue: 0, // Torna invisível
        duration: 250,
        useNativeDriver: true,
        easing: Easing.in(Easing.cubic),
      }),
      Animated.timing(modalPosition, {
        toValue: screenHeight,
        duration: 300,
        useNativeDriver: true,
        easing: Easing.in(Easing.cubic),
      }),
    ]).start(() => {
      setModalVisible(false);
      callback?.();
    });
  };

  const selectAndApplyFilter = (field: SearchField): void => {
    hideModal(() => {
      onFilterChange(field);
    });
  };

  const panResponder = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => true,
      onPanResponderMove: (_, gestureState) => {
        if (gestureState.dy > 0) {
          modalPosition.setValue(gestureState.dy);
        }
      },
      onPanResponderRelease: (_, gestureState) => {
        if (gestureState.dy > 150) {
          hideModal();
        } else {
          Animated.spring(modalPosition, {
            toValue: 0,
            useNativeDriver: true,
          }).start();
        }
      },
      onPanResponderTerminate: (_, gestureState) => {
        if (gestureState.dy > 150) {
          hideModal();
        } else {
          Animated.spring(modalPosition, {
            toValue: 0,
            useNativeDriver: true,
          }).start();
        }
      },
    })
  ).current;

  return (
    <View style={styles.container}>
      <TouchableOpacity
        onPress={() => setModalVisible(true)}
        style={styles.filterButton}
        activeOpacity={0.8}
        accessibilityLabel="Open filter options"
      >
        <Image
          source={require('./icons/filtro.png')}
          style={styles.filterIcon}
          resizeMode="contain"
          accessibilityIgnoresInvertColors
        />
      </TouchableOpacity>

      <Modal
        animationType="none"
        transparent={true}
        visible={modalVisible}
        onRequestClose={() => hideModal()}
        statusBarTranslucent={true}
      >
        <View style={styles.modalContainer}>
          <Animated.View
            style={[styles.overlay, { opacity: backdropOpacity }]}
          >
            <Pressable
              style={StyleSheet.absoluteFill}
              onPress={() => hideModal()}
            />
          </Animated.View>

          <Animated.View
            style={[styles.modalContent, { transform: [{ translateY: modalPosition }], opacity: modalOpacity }]} // Adiciona opacidade
            {...panResponder.panHandlers}
          >
            <View style={styles.handleBar} />
            <Text style={styles.modalTitle}>Opções de Filtro</Text>
            {filterOptions.map((option) => (
              <TouchableOpacity
                key={option.id}
                onPress={() => selectAndApplyFilter(option.field)}
                style={[styles.optionButton, currentFilterField === option.field && styles.selectedOptionButton]}
                accessibilityLabel={`Filter by ${option.name}`}
                accessibilityRole="button"
              >
                <Text style={[styles.optionText, currentFilterField === option.field && styles.selectedOptionText]}>
                  {option.name}
                </Text>
              </TouchableOpacity>
            ))}
            <View style={styles.modalActions}>
              <Pressable
                onPress={() => hideModal()}
                style={styles.cancelButton}
                accessibilityLabel="Cancel filter selection"
                accessibilityRole="button"
              >
                <Text style={styles.cancelButtonText}>Cancelar</Text>
              </Pressable>
            </View>
          </Animated.View>
        </View>
      </Modal>
    </View>
  );
};
// Estilos
const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    padding: 12,
  },
  filterButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#818cf8',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 8,
  },
  filterIcon: {
    width: 20,
    height: 28,
  },
  modalContainer: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  overlay: {
    position: 'absolute',
    top: 0,
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: 'rgba(0,0,0,0.5)',
  },
  modalContent: {
    backgroundColor: 'white',
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    borderBottomRightRadius: 20,
    borderBottomLeftRadius: 20,
    padding: 24,
    paddingTop: 16,
    elevation: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: -2 },
    shadowOpacity: 0.2,
    shadowRadius: 5,
  },
  handleBar: {
    width: 40,
    height: 4,
    backgroundColor: '#d1d5db',
    borderRadius: 4,
    alignSelf: 'center',
    marginBottom: 16,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 16,
  },
  optionButton: {
    padding: 12,
    marginBottom: 8,
    borderWidth: 1,
    borderRadius: 8,
    borderColor: '#d1d5db',
  },
  selectedOptionButton: {
    backgroundColor: '#eff6ff',
    borderColor: '#60a5fa',
  },
  optionText: {
    color: '#374151',
  },
  selectedOptionText: {
    color: '#60a5fa',
  },
  modalActions: {
    flexDirection: 'row',
    justifyContent: 'center',
    marginTop: 16,
  },
  cancelButton: {
    backgroundColor: '#e5e7eb',
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 8,
  },
  cancelButtonText: {
    color: '#374151',
    fontWeight: '500',
  },
});

export default FilterButton;