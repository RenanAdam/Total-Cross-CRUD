package com.softsite.tccrud.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.softsite.tccrud.exceptions.RecordAlreadyExists;
import com.softsite.tccrud.exceptions.RecordNotExists;
import com.softsite.tccrud.exceptions.ThereAreNoRecords;
import com.softsite.tccrud.model.Endereco;

import totalcross.sql.Connection;
import totalcross.sql.ResultSet;
import totalcross.sql.Statement;

public class EnderecoDAO {
	
	public void createIfNotExistTable(Connection dbcon) throws SQLException {
	    Statement conn = dbcon.createStatement();
	    conn.execute("CREATE TABLE IF NOT EXISTS endereco "
	    		+ "(id integer primary key, nome varchar, endereco varchar , bairro varchar, cidade varchar, estado varchar)");
	    conn.close();
	}
	
    public Endereco selectById(Long id, Connection dbcon) throws SQLException {
        Statement conn = dbcon.createStatement();
        
        String query = "SELECT id, nome , bairro, cidade, estado FROM endereco WHERE id = '" + id + "'";
        ResultSet rs = conn.executeQuery(query);

        Endereco ev = new Endereco();
        while (rs.next()) {
            ev.setId(rs.getLong(1));
        	ev.setNome(rs.getString(2));
            ev.setBairro(rs.getString(3));
            ev.setCidade(rs.getString(4));
            ev.setEstado(rs.getString(5));
        }
        
        return ev;
    }
	
	public void insert(Endereco endereco, Connection dbcon) throws SQLException, RecordAlreadyExists {
	    Statement conn = dbcon.createStatement();
	
//		Endereco endereco;
//		endereco = selectById(endereco.getId(), dbcon);
		
		if (endereco.getId() == null) {
		    conn.executeUpdate("INSERT INTO endereco (nome,endereco,bairro,cidade,estado) "
		    		+ "VALUES ('" + endereco.getNome()  + "','" + endereco.getEndereco() + "','" + endereco.getBairro() + "','" + endereco.getCidade() + "','" + endereco.getEstado() + "')");            
		} else {
		    throw new RecordAlreadyExists();
		}
		
		conn.close();
	}
	
	public List<Endereco> listFilter(Endereco filter, Connection dbcon) throws SQLException {
		List<Endereco> enderecoLista = new ArrayList<Endereco>();
		Endereco enderecoNovo;
		
		String where = "";
		if(!filter.getNome().isEmpty()){
			where += " WHERE nome like '%" + filter.getNome() + "%'";
		}
		
        Statement conn = dbcon.createStatement();
        String query = "SELECT id , nome , endereco , bairro, cidade, estado "
        		+ "FROM endereco "
        		+ where
        		+ "ORDER BY nome";
        ResultSet rs = conn.executeQuery(query);
        
        while (rs.next()) {
        	enderecoNovo = new Endereco();
        	enderecoNovo.setId(Long.parseLong(rs.getString(1)));
        	enderecoNovo.setNome(rs.getString(2));
        	enderecoNovo.setEndereco(rs.getString(3));
        	enderecoNovo.setBairro(rs.getString(4));
        	enderecoNovo.setCidade(rs.getString(5));
        	enderecoNovo.setEstado(rs.getString(6));
        	enderecoLista.add(enderecoNovo);
        }
        
        if (enderecoLista.size() == 0){
            return null;
        }
        return enderecoLista;
	}
	
	public List<Endereco> listAll(Connection dbcon) throws SQLException, ThereAreNoRecords {
		List<Endereco> enderecoLista = new ArrayList<Endereco>();
		Endereco enderecoNovo;
		
        Statement conn = dbcon.createStatement();
        String query = "SELECT id , nome , endereco , bairro, cidade, estado FROM endereco ORDER BY nome";
        ResultSet rs = conn.executeQuery(query);
        
        while (rs.next()) {
        	enderecoNovo = new Endereco();
        	enderecoNovo.setId(Long.parseLong(rs.getString(1)));
        	enderecoNovo.setNome(rs.getString(2));
        	enderecoNovo.setEndereco(rs.getString(3));
        	enderecoNovo.setBairro(rs.getString(4));
        	enderecoNovo.setCidade(rs.getString(5));
        	enderecoNovo.setEstado(rs.getString(6));
        	enderecoLista.add(enderecoNovo);
        }
        
        if (enderecoLista.size() == 0){
            throw new ThereAreNoRecords();
        }
        return enderecoLista;
	}
	
	public void update(Endereco endereco, Connection dbcon) throws SQLException, RecordNotExists {
        
		Statement conn = dbcon.createStatement();

        Endereco enderecoBusca;
        enderecoBusca = selectById(endereco.getId(), dbcon);

        if (enderecoBusca.getId() != null){
            conn.executeUpdate("UPDATE endereco SET nome = '" + endereco.getNome() + "', "
            		+ " endereco = '" + endereco.getEndereco() + "', "
            		+ " bairro = '" + endereco.getBairro() + "' , "
            		+ " cidade = '" + endereco.getCidade() + "' , "
            		+ " estado = '" + endereco.getEstado() + "' "
            		+ " WHERE id = '" + endereco.getId() + "'");
        }else{
            throw new RecordNotExists();
        }
        conn.close();
	}
	
    public void delete(Endereco endereco, Connection dbcon) throws SQLException, RecordNotExists {
        
    	Statement conn = dbcon.createStatement();

        if (endereco.getId() != null){
            conn.executeUpdate("DELETE FROM endereco WHERE id = '" + endereco.getId() + "'");
        }else{
            throw new RecordNotExists();
        }
        conn.close();
    }
}
