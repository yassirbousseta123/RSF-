import React, { useState } from 'react';
import {
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TableSortLabel,
  Toolbar,
  Typography,
  TextField,
  MenuItem,
  Chip,
  TablePagination,
  Alert,
  CircularProgress,
  IconButton,
  Tooltip
} from '@mui/material';
import FilterListIcon from '@mui/icons-material/FilterList';
import SearchIcon from '@mui/icons-material/Search';
import FixIcon from '@mui/icons-material/Build';
import { ValidationError, ValidationErrorType } from '../../types/validation';
import { visuallyHidden } from '@mui/utils';

interface Column {
  id: keyof ValidationError | 'actions';
  label: string;
  minWidth?: number;
  align?: 'right' | 'left' | 'center';
  format?: (value: any) => string | React.ReactNode;
}

const columns: Column[] = [
  { id: 'fileName', label: 'File Name', minWidth: 120 },
  { id: 'lineNumber', label: 'Line', minWidth: 70, align: 'right' },
  { id: 'fieldName', label: 'Field', minWidth: 100 },
  { id: 'errorType', label: 'Error Type', minWidth: 130,
    format: (value: ValidationErrorType) => (
      <Chip 
        label={value} 
        size="small"
        color={
          value === 'DATA_ERROR' || value === 'FORMAT_ERROR' ? 'warning' :
          value === 'STRUCTURAL' || value === 'STRUCTURAL_ERROR' ? 'error' :
          value === 'SYSTEM_ERROR' ? 'error' :
          'default'
        }
      />
    )
  },
  { id: 'message', label: 'Message', minWidth: 200 },
  { id: 'recordIdentifier', label: 'Record ID', minWidth: 100 },
  { id: 'timestamp', label: 'Time', minWidth: 120, 
    format: (value: string) => new Date(value).toLocaleString() 
  },
  { id: 'actions', label: 'Actions', minWidth: 70, align: 'center' }
];

const errorTypes: ValidationErrorType[] = [
  'DATA_ERROR',
  'STRUCTURAL',
  'SEQUENCE_ERROR',
  'DEPENDENCY_ERROR',
  'FILE_NAME_ERROR',
  'STRUCTURAL_ERROR',
  'FORMAT_ERROR',
  'SYSTEM_ERROR'
];

interface ValidationErrorTableProps {
  errors: ValidationError[];
  loading?: boolean;
  onFixError?: (errorId: string) => void;
  totalItems?: number;
  page?: number;
  rowsPerPage?: number;
  onPageChange?: (page: number) => void;
  onRowsPerPageChange?: (rowsPerPage: number) => void;
}

function ValidationErrorTable({
  errors,
  loading = false,
  onFixError,
  totalItems = 0,
  page = 0,
  rowsPerPage = 10,
  onPageChange,
  onRowsPerPageChange
}: ValidationErrorTableProps) {
  const [orderBy, setOrderBy] = useState<keyof ValidationError>('timestamp');
  const [order, setOrder] = useState<'asc' | 'desc'>('desc');
  const [showFilters, setShowFilters] = useState(false);
  const [filters, setFilters] = useState({
    fileName: '',
    errorType: '',
    fieldName: '',
    recordId: ''
  });

  const handleRequestSort = (property: keyof ValidationError) => {
    const isAsc = orderBy === property && order === 'asc';
    setOrder(isAsc ? 'desc' : 'asc');
    setOrderBy(property);
  };

  const handleFilterChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setFilters({
      ...filters,
      [event.target.name]: event.target.value
    });
  };

  const handleChangePage = (_: unknown, newPage: number) => {
    if (onPageChange) {
      onPageChange(newPage);
    }
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (onRowsPerPageChange) {
      onRowsPerPageChange(parseInt(event.target.value, 10));
    }
  };

  return (
    <Paper sx={{ width: '100%', overflow: 'hidden' }}>
      <Toolbar sx={{ pl: { sm: 2 }, pr: { xs: 1, sm: 1 }, justifyContent: 'space-between' }}>
        <Typography variant="h6" id="tableTitle" component="div">
          Validation Errors
        </Typography>
        <Tooltip title="Filter errors">
          <IconButton onClick={() => setShowFilters(!showFilters)}>
            <FilterListIcon />
          </IconButton>
        </Tooltip>
      </Toolbar>

      {showFilters && (
        <Box sx={{ p: 2, display: 'flex', flexWrap: 'wrap', gap: 2 }}>
          <TextField
            label="File Name"
            name="fileName"
            value={filters.fileName}
            onChange={handleFilterChange}
            variant="outlined"
            size="small"
            InputProps={{
              startAdornment: <SearchIcon fontSize="small" sx={{ mr: 1, color: 'action.active' }} />
            }}
          />
          <TextField
            select
            label="Error Type"
            name="errorType"
            value={filters.errorType}
            onChange={handleFilterChange}
            variant="outlined"
            size="small"
            sx={{ minWidth: 200 }}
          >
            <MenuItem value="">All Types</MenuItem>
            {errorTypes.map((type) => (
              <MenuItem key={type} value={type}>{type}</MenuItem>
            ))}
          </TextField>
          <TextField
            label="Field Name"
            name="fieldName"
            value={filters.fieldName}
            onChange={handleFilterChange}
            variant="outlined"
            size="small"
          />
          <TextField
            label="Record ID"
            name="recordId"
            value={filters.recordId}
            onChange={handleFilterChange}
            variant="outlined"
            size="small"
          />
        </Box>
      )}

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 3 }}>
          <CircularProgress />
        </Box>
      ) : errors.length === 0 ? (
        <Alert severity="info" sx={{ m: 2 }}>No errors found.</Alert>
      ) : (
        <>
          <TableContainer sx={{ maxHeight: 640 }}>
            <Table stickyHeader aria-label="validation errors table">
              <TableHead>
                <TableRow>
                  {columns.map((column) => (
                    <TableCell
                      key={column.id}
                      align={column.align}
                      style={{ minWidth: column.minWidth, fontWeight: 'bold' }}
                    >
                      {column.id !== 'actions' ? (
                        <TableSortLabel
                          active={orderBy === column.id}
                          direction={orderBy === column.id ? order : 'asc'}
                          onClick={() => handleRequestSort(column.id as keyof ValidationError)}
                        >
                          {column.label}
                          {orderBy === column.id ? (
                            <Box component="span" sx={visuallyHidden}>
                              {order === 'desc' ? 'sorted descending' : 'sorted ascending'}
                            </Box>
                          ) : null}
                        </TableSortLabel>
                      ) : (
                        column.label
                      )}
                    </TableCell>
                  ))}
                </TableRow>
              </TableHead>
              <TableBody>
                {errors.map((error) => (
                  <TableRow hover role="checkbox" tabIndex={-1} key={error.id}>
                    {columns.map((column) => {
                      if (column.id === 'actions') {
                        return (
                          <TableCell key={column.id} align={column.align}>
                            {onFixError && (
                              <Tooltip title="Attempt to fix this error">
                                <IconButton 
                                  size="small"
                                  onClick={() => onFixError(error.id)}
                                  color="primary"
                                >
                                  <FixIcon fontSize="small" />
                                </IconButton>
                              </Tooltip>
                            )}
                          </TableCell>
                        );
                      }
                      
                      const value = error[column.id as keyof ValidationError];
                      return (
                        <TableCell key={column.id} align={column.align}>
                          {column.format && value !== null && value !== undefined
                            ? column.format(value)
                            : value}
                        </TableCell>
                      );
                    })}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          <TablePagination
            rowsPerPageOptions={[10, 25, 50, 100]}
            component="div"
            count={totalItems}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={handleChangePage}
            onRowsPerPageChange={handleChangeRowsPerPage}
          />
        </>
      )}
    </Paper>
  );
}

export default ValidationErrorTable; 