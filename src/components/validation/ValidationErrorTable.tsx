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
  TablePagination, 
  TableSortLabel,
  Chip,
  IconButton,
  Tooltip,
  TextField,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  CircularProgress
} from '@mui/material';
import { 
  FilterAlt as FilterIcon,
  Search as SearchIcon, 
  Refresh as RefreshIcon,
  BuildCircle as FixIcon 
} from '@mui/icons-material';
import { ValidationError, ValidationErrorType } from '../../types/validation';

// Define sort orders type
type Order = 'asc' | 'desc';

// Define column types
type Column = {
  id: keyof ValidationError | 'actions';
  label: string;
  minWidth?: number;
  align?: 'right' | 'left' | 'center';
  format?: (value: any) => React.ReactNode;
};

// Define props for the component
interface ValidationErrorTableProps {
  errors: ValidationError[];
  loading: boolean;
  onFixError?: (errorId: string) => Promise<void>;
  onRefresh?: () => void;
}

const ValidationErrorTable: React.FC<ValidationErrorTableProps> = ({
  errors,
  loading,
  onFixError,
  onRefresh
}) => {
  // State for pagination, sorting, and filtering
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [orderBy, setOrderBy] = useState<keyof ValidationError>('fileName');
  const [order, setOrder] = useState<Order>('asc');
  const [filterType, setFilterType] = useState<ValidationErrorType | 'ALL'>('ALL');
  const [searchTerm, setSearchTerm] = useState('');
  const [fixingErrorId, setFixingErrorId] = useState<string | null>(null);

  // Define columns
  const columns: Column[] = [
    { id: 'fileName', label: 'File Name', minWidth: 150 },
    { id: 'recordIdentifier', label: 'Record ID', minWidth: 100 },
    { id: 'lineNumber', label: 'Line', minWidth: 50, align: 'right' },
    { id: 'fieldName', label: 'Field', minWidth: 120 },
    { 
      id: 'errorType', 
      label: 'Error Type', 
      minWidth: 140,
      format: (value: ValidationErrorType) => (
        <Chip 
          label={value.replace('_', ' ')} 
          color={
            value === 'STRUCTURAL' ? 'error' : 
            value === 'DATA_ERROR' ? 'warning' : 
            value === 'SEQUENCE_ERROR' ? 'info' : 
            'default'
          }
          size="small"
        />
      )
    },
    { id: 'message', label: 'Message', minWidth: 200 },
    { 
      id: 'actions', 
      label: 'Actions', 
      minWidth: 100,
      align: 'center',
      format: (error: ValidationError) => (
        <Box>
          {onFixError && (
            <Tooltip title="Attempt to fix this error">
              <span>
                <IconButton 
                  size="small" 
                  onClick={() => handleFixError(error.id)}
                  disabled={fixingErrorId === error.id}
                >
                  {fixingErrorId === error.id ? (
                    <CircularProgress size={20} />
                  ) : (
                    <FixIcon fontSize="small" />
                  )}
                </IconButton>
              </span>
            </Tooltip>
          )}
        </Box>
      )
    }
  ];

  // Handle sorting column change
  const handleRequestSort = (property: keyof ValidationError) => {
    const isAsc = orderBy === property && order === 'asc';
    setOrder(isAsc ? 'desc' : 'asc');
    setOrderBy(property);
  };

  // Handle page change
  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  // Handle rows per page change
  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(+event.target.value);
    setPage(0);
  };

  // Handle filter type change
  const handleFilterTypeChange = (event: any) => {
    setFilterType(event.target.value);
    setPage(0);
  };

  // Handle search term change
  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(event.target.value);
    setPage(0);
  };

  // Handle fixing an error
  const handleFixError = async (errorId: string) => {
    if (onFixError) {
      setFixingErrorId(errorId);
      try {
        await onFixError(errorId);
      } finally {
        setFixingErrorId(null);
      }
    }
  };

  // Filter errors based on filter type and search term
  const filteredErrors = errors.filter(error => {
    // Filter by error type
    if (filterType !== 'ALL' && error.errorType !== filterType) {
      return false;
    }

    // Filter by search term across multiple fields
    if (searchTerm) {
      const searchLower = searchTerm.toLowerCase();
      return (
        (error.fileName && error.fileName.toLowerCase().includes(searchLower)) ||
        (error.fieldName && error.fieldName.toLowerCase().includes(searchLower)) ||
        (error.message && error.message.toLowerCase().includes(searchLower)) ||
        (error.recordIdentifier && error.recordIdentifier.toLowerCase().includes(searchLower))
      );
    }

    return true;
  });

  // Sort errors
  const sortedErrors = [...filteredErrors].sort((a, b) => {
    const aValue = a[orderBy];
    const bValue = b[orderBy];

    // Handle null or undefined values
    if (!aValue && aValue !== 0) return order === 'asc' ? -1 : 1;
    if (!bValue && bValue !== 0) return order === 'asc' ? 1 : -1;

    // Sort strings
    if (typeof aValue === 'string' && typeof bValue === 'string') {
      return order === 'asc' 
        ? aValue.localeCompare(bValue) 
        : bValue.localeCompare(aValue);
    }

    // Sort numbers
    return order === 'asc' 
      ? (aValue < bValue ? -1 : aValue > bValue ? 1 : 0)
      : (bValue < aValue ? -1 : bValue > aValue ? 1 : 0);
  });

  // Get paginated data
  const paginatedErrors = sortedErrors.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage
  );

  // No data label
  const getEmptyRowsLabel = () => {
    if (loading) {
      return 'Loading validation errors...';
    }
    if (errors.length === 0) {
      return 'No validation errors found';
    }
    return 'No matching errors found';
  };

  return (
    <Paper sx={{ width: '100%', overflow: 'hidden' }}>
      {/* Filter Bar */}
      <Box sx={{ p: 2, display: 'flex', gap: 2, flexWrap: 'wrap' }}>
        <FormControl variant="outlined" size="small" sx={{ minWidth: 150 }}>
          <InputLabel id="error-type-filter-label">Error Type</InputLabel>
          <Select
            labelId="error-type-filter-label"
            id="error-type-filter"
            value={filterType}
            onChange={handleFilterTypeChange}
            label="Error Type"
            startAdornment={<FilterIcon fontSize="small" sx={{ mr: 1 }} />}
          >
            <MenuItem value="ALL">All Types</MenuItem>
            <MenuItem value="DATA_ERROR">Data Error</MenuItem>
            <MenuItem value="STRUCTURAL">Structural</MenuItem>
            <MenuItem value="SEQUENCE_ERROR">Sequence Error</MenuItem>
            <MenuItem value="REFERENCE_ERROR">Reference Error</MenuItem>
            <MenuItem value="CONSISTENCY_ERROR">Consistency Error</MenuItem>
            <MenuItem value="FORMAT_ERROR">Format Error</MenuItem>
            <MenuItem value="OTHER">Other</MenuItem>
          </Select>
        </FormControl>

        <TextField
          size="small"
          variant="outlined"
          label="Search"
          value={searchTerm}
          onChange={handleSearchChange}
          sx={{ flexGrow: 1 }}
          InputProps={{
            startAdornment: <SearchIcon fontSize="small" sx={{ mr: 1 }} />
          }}
        />

        {onRefresh && (
          <Tooltip title="Refresh error list">
            <IconButton onClick={onRefresh}>
              <RefreshIcon />
            </IconButton>
          </Tooltip>
        )}
      </Box>

      {/* Error Table */}
      <TableContainer sx={{ maxHeight: 440 }}>
        <Table stickyHeader aria-label="validation errors table">
          <TableHead>
            <TableRow>
              {columns.map((column) => (
                <TableCell
                  key={column.id}
                  align={column.align}
                  style={{ minWidth: column.minWidth }}
                  sortDirection={orderBy === column.id ? order : false}
                >
                  {column.id !== 'actions' ? (
                    <TableSortLabel
                      active={orderBy === column.id}
                      direction={orderBy === column.id ? order : 'asc'}
                      onClick={() => handleRequestSort(column.id as keyof ValidationError)}
                    >
                      {column.label}
                    </TableSortLabel>
                  ) : (
                    column.label
                  )}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={columns.length} align="center">
                  <CircularProgress />
                </TableCell>
              </TableRow>
            ) : paginatedErrors.length > 0 ? (
              paginatedErrors.map((error) => (
                <TableRow hover tabIndex={-1} key={error.id}>
                  {columns.map((column) => {
                    const value = column.id === 'actions' ? error : error[column.id as keyof ValidationError];
                    return (
                      <TableCell key={column.id} align={column.align}>
                        {column.format ? column.format(value) : value}
                      </TableCell>
                    );
                  })}
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={columns.length} align="center">
                  {getEmptyRowsLabel()}
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Pagination */}
      <TablePagination
        rowsPerPageOptions={[10, 25, 50, 100]}
        component="div"
        count={filteredErrors.length}
        rowsPerPage={rowsPerPage}
        page={page}
        onPageChange={handleChangePage}
        onRowsPerPageChange={handleChangeRowsPerPage}
      />
    </Paper>
  );
};

export default ValidationErrorTable; 