package com.softsite.tccrud.view;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.softsite.tccrud.dao.EnderecoDAO;
import com.softsite.tccrud.exceptions.RecordAlreadyExists;
import com.softsite.tccrud.exceptions.RecordNotExists;
import com.softsite.tccrud.exceptions.ThereAreNoRecords;
import com.softsite.tccrud.model.Endereco;

import totalcross.io.IOException;
import totalcross.sql.Connection;
import totalcross.sql.DriverManager;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.ui.AlignedLabelsContainer;
import totalcross.ui.Bar;
import totalcross.ui.Button;
import totalcross.ui.ComboBox;
import totalcross.ui.Edit;
import totalcross.ui.Grid;
import totalcross.ui.MainWindow;
import totalcross.ui.Spacer;
import totalcross.ui.Toast;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.GridEvent;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.InvalidDateException;

public class Main extends MainWindow {
	private Connection conn;
	private EnderecoDAO enderecoDao;
	private Edit fieldId, fieldNome, fieldEndereco, fieldBairro, fieldCidade;
	private ComboBox fieldEstado;
	private MessageBox boxConfirmacao,boxAjuda;
	private Button buttonInsertUpdate, buttonClear, buttonDelete,buttonFilter;
	private Image imageExit,imageHelp,imageSave,imageDelete,imageSearch,imageClear;
	private Bar bar;
	private Grid grid;

	public Main() {
		super("Lista de Endereços", VERTICAL_GRADIENT);

		gradientTitleStartColor = 0;
		gradientTitleEndColor = 0xAAAAFF;

		setUIStyle(Settings.Android);
		Settings.uiAdjustmentsBasedOnFontHeight = true;
		setBackColor(0xDDDDFF);
		
		//Inicializando o atributo de conexao e o atributo DAO
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:" + Convert.appendPath(Settings.appPath, "database.db"));
			enderecoDao = new EnderecoDAO();
			enderecoDao.createIfNotExistTable(conn);
		} catch (Exception e) {
			MessageBox.showException(e, true);
			exit(0);
		}
		
		//Inicializando as imagens
		try {
			imageExit = new Image("sair.png");
			imageHelp = new Image("ajuda.png");
			imageSave = new Image("salvar.png");
			imageDelete = new Image("deletar.png");
			imageSearch = new Image("buscar.png");
			imageClear = new Image("limpar.png");
		} catch (ImageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Inicializando o Bar
		Font myFont = Font.getFont(true,Font.NORMAL_SIZE+2);
		bar = new Bar("Endereços");
		bar.canSelectTitle = true;
		bar.setFont(myFont);
		bar.setBackForeColors(0x0A246A,Color.WHITE);
		bar.addButton(imageHelp);
		bar.addButton(imageExit);
		
		//Inicializando os campos do formulario
		fieldId = new Edit();
		fieldNome = new Edit();
		fieldEndereco = new Edit();
		fieldBairro = new Edit();
		fieldCidade = new Edit();
		String[] estados = new String[] {"AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MT","MS","MG","PA","PB","PR","PE","PI","RJ","RN","RS","RO","RR","SC","SP","SE","TO"};
		fieldEstado = new ComboBox(estados); 
		
		//Inicializando os botoes
		buttonInsertUpdate = new Button(imageSave);
		buttonClear = new Button(imageClear);
		buttonDelete = new Button(imageDelete);
		buttonFilter = new Button(imageSearch);
		
		//Inicializando a Grid
		String[] gridCaptions = { " ID ", " Nome ", " Endereço ", " Bairro " , " Cidade " , " UF " };
		int gridWidths[] = {-10, -40, -50,-30,-30,-8};
        int gridAligns[] = {CENTER,LEFT,LEFT,LEFT,LEFT,CENTER};
		Grid.useHorizontalScrollBar = true;
		grid = new Grid(gridCaptions,gridWidths,gridAligns, false);
		Grid.useHorizontalScrollBar = true;
		grid.setRect(CENTER, BOTTOM - 110, PARENTSIZE + 100, PREFERRED - 230);
	}

	@Override
	public void initUI() {

		//Adicionando um Bar no topo da tela
		add(bar, LEFT,0,FILL,PREFERRED);
		
		//Criando um Container para os campos do formulario
		String[] labels = { "Nome", "Endereço", "Bairro", "Cidade", "Estado" };
		AlignedLabelsContainer c = new AlignedLabelsContainer(labels);
		c.setBorderStyle(BORDER_LOWERED);
		c.labelAlign = RIGHT;
		c.foreColors = new int[] { Color.RED, Color.RED, Color.RED, Color.RED, Color.RED};
		c.setInsets(2, 2, 2, 2);
		c.setFont(font.asBold());
		c.childrenFont = font;
		add(c, LEFT + 2, TOP + 60, FILL - 2, PREFERRED + 100);
		
		//Adicionando os campos ao Container do formulario
		c.add(fieldNome, LEFT + 2, AFTER + 2);
		c.add(fieldEndereco, LEFT + 2, AFTER + 0);
		c.add(fieldBairro, LEFT + 2, AFTER + 2);
		c.add(fieldCidade, LEFT + 2, AFTER + 0);
		c.add(fieldEstado, LEFT + 2, AFTER);
		
		//Adicionando os botoes de "Confirmar" (insert/update) e "Limpar" campos ao Container
		c.add(buttonInsertUpdate, LEFT, AFTER + 2);
		c.add(buttonFilter, CENTER, SAME);
		c.add(buttonClear, RIGHT, SAME);
		
		//Adicionando o botao "Excluir" ao Container principal
		Spacer sp = new Spacer(0, 0);
		add(sp, CENTER, BOTTOM - 150, PARENTSIZE + 10, PREFERRED);
		add(buttonDelete, CENTER, SAME, sp);

		try {
			listAll();
		} catch (Exception e) {
			MessageBox.showException(e, true);
			exit(0);
		}

		//Adicionando o mostrar mensagens de respostas as acoes do usuario
		Toast.posY = CENTER;
	}

	@Override
	public void onEvent(Event e){
		try {
			switch (e.type) {
			case ControlEvent.PRESSED:
				if (e.target == buttonInsertUpdate) {
					insertUpdate();
				} else if (e.target == buttonClear) {
					clean();
				} else if (e.target == buttonFilter) {//Listar os enderecos na Grid pelo filtro
					if(fieldNome.getLength() == 0){
						Toast.show("Digite o nome para pesquisar!", 3000);
					}else{
						Endereco endereco = getEnderecoFromFields();
						List<Endereco> enderecoLista = enderecoDao.listFilter(endereco, conn);
						if(enderecoLista != null){
							setGrid(enderecoLista);
						}else{
							Toast.show("A busca retornou zero registros!", 3000);
						}	
					}
				} else if (e.target == buttonDelete) {
					delete();
				}else if (e.target == bar){
					if(bar.getSelectedIndex() == 1){//Mostrar tela de ajuda
						boxAjuda = new MessageBox("Ajuda","Aplicativo para armazenar a lista de endereços dos seus contatos.");
						boxAjuda.setDelayToShowButton(100);
						boxAjuda.popup();
					}else if(bar.getSelectedIndex() == 2){//Sair da aplicacao
						exit(0);
					}
				}
				break;
			case GridEvent.SELECTED_EVENT:
				if (e.target == grid) {
					int idx = grid.getSelectedIndex();

					if (idx == -1) {
						Toast.show("Selecione um registro na lista.", 3000);
					} else {
						/*Setando os valores dos campos do formulario 
						 com os valores dos campos da linha selecionada na Grid*/
						String[] linhaGrid = grid.getItem(idx);
						fieldId.setText(linhaGrid[0]);
						fieldNome.setText(linhaGrid[1]);
						fieldEndereco.setText(linhaGrid[2]);
						fieldBairro.setText(linhaGrid[3]);
						fieldCidade.setText(linhaGrid[4]);
						fieldEstado.setSelectedItem(linhaGrid[5]);
					}
				}
				break;
			}
		} catch (Exception ee) {
			MessageBox.showException(ee, true);
		}
	}

	private void insertUpdate() throws SQLException, InvalidDateException {

		//Validacao de todos os campos como obrigatorios
		if (fieldNome.getLength() == 0 || fieldEndereco.getLength() == 0 || fieldBairro.getLength() == 0 
				|| fieldCidade.getLength() == 0 || fieldEstado.getSelectedIndex() == -1) {
			Toast.show("Preencha todos os campos!", 3000);
		} else {
			Endereco endereco = getEnderecoFromFields();
			
			try {
				if (fieldId.getLength() != 0) {
					endereco.setId(Long.parseLong(fieldId.getText()));
					enderecoDao.update(endereco, conn);
					Toast.show("Registro atualizado com sucesso!", 3000);
				} else {
					enderecoDao.insert(endereco, conn);
					Toast.show("Registro cadastrado com sucesso!", 3000);
				}
			} catch (RecordAlreadyExists e) {
				// TODO: handle exception
				Toast.show("Registro já existente!", 3000);
			} catch (RecordNotExists e) {
				Toast.show("Registro não encontrado!", 3000);
			} finally {
				clean();
				listAll();
			}
		}
	}

	//Lista na Grid todos os registros cadastrados no banco
	private void listAll() throws SQLException {
		List<Endereco> enderecoLista = new ArrayList<Endereco>();
		try {
			enderecoLista = enderecoDao.listAll(conn);
			add(grid);
			setGrid(enderecoLista);
		} catch (ThereAreNoRecords e) {
			// TODO: handle exception
			Toast.show("Nenhum registro encontrado!", 3000);
		}
	}

	private void delete() throws SQLException, InvalidDateException {

		try{
			int idx = grid.getSelectedIndex();
			if (idx == -1) {
				Toast.show("Nenhum registro foi selecionado!", 3000);
			} else {

				//Mostrando um MessageBox de confirmacao de exclusao do registro
				String[] buttonCaptions = new String[] {"Sim", "Não"};
				boxConfirmacao = new MessageBox("Confirmar","Deseja realmente excluir o registro?", buttonCaptions);
				boxConfirmacao.setDelayToShowButton(100);
				boxConfirmacao.popup();
				int botaoConfirmacao = boxConfirmacao.getPressedButtonIndex();
				
				if(botaoConfirmacao == 0){
					Endereco endereco = new Endereco();
					String[] linhaGrid = grid.getItem(idx);
					endereco.setId(Long.parseLong(linhaGrid[0]));
					endereco.setNome(linhaGrid[1]);
					endereco.setEndereco(linhaGrid[2]);
					endereco.setBairro(linhaGrid[3]);
					endereco.setCidade(linhaGrid[4]);
					endereco.setEstado(linhaGrid[5]);
					
					try {
						enderecoDao.delete(endereco, conn);
						grid.del(idx);
						Toast.show("Registro excluído com sucesso!", 3000);
					} catch (RecordNotExists e) {
						// TODO: handle exception
						Toast.show("Nenhum registro foi encontrado!", 3000);
					} finally {
						clean();
						repaint();
					}
				}
			}
		}catch(NullPointerException e){
			Toast.show("Nenhum registro foi selecionado!", 3000);
		}	
	}

	//Povoando o modelo Endereco com os valores dos campos d formulario
	private Endereco getEnderecoFromFields(){
		Endereco endereco = new Endereco();
		endereco.setNome(fieldNome.getText());
		endereco.setEndereco(fieldEndereco.getText());
		endereco.setBairro(fieldBairro.getText());
		endereco.setCidade(fieldCidade.getText());
		endereco.setEstado(fieldEstado.getSelectedItem().toString());
		return endereco;
	}

	//Povoando a Grid apartir de uma lista de enderecos
	private void setGrid(List<Endereco> enderecoLista){
		String items[][] = new String[enderecoLista.size()][7];
		for (int i = 0; i < enderecoLista.size(); i++) {
			items[i][0] = enderecoLista.get(i).getId().toString();
			items[i][1] = enderecoLista.get(i).getNome();
			items[i][2] = enderecoLista.get(i).getEndereco();
			items[i][3] = enderecoLista.get(i).getBairro();
			items[i][4] = enderecoLista.get(i).getCidade();
			items[i][5] = enderecoLista.get(i).getEstado();
		}
		grid.setItems(items);
	}
	
	private void clean() {
		fieldId.clear();
		//@TODO limpar combobox corretamente
		fieldEstado.clear();
		clear();
	}
}
